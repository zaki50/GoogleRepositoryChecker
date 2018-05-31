package org.zakky.googlerepositorychecker.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import org.zakky.googlerepositorychecker.MyApplication
import org.zakky.googlerepositorychecker.R
import org.zakky.googlerepositorychecker.model.Artifact
import org.zakky.googlerepositorychecker.model.Group
import org.zakky.googlerepositorychecker.realm.opGetAllGroupsOrderedByName
import org.zakky.googlerepositorychecker.realm.opToggleFavorite
import org.zakky.googlerepositorychecker.ui.recyclerview.ItemDividerDecoration
import toothpick.Toothpick
import javax.inject.Inject

class AllGroupsFragment : Fragment() {
    companion object {
        private const val STATE_QUERY_STRING = "queryString"

        fun newInstance() = AllGroupsFragment()

        private val ATTRS = intArrayOf(
                R.attr.colorAccent
        )
    }

    @Inject
    lateinit var realm: Realm

    private lateinit var list: RecyclerView

    private lateinit var queryString: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Toothpick.inject(this, Toothpick.openScope(MyApplication.APP_SCOPE_NAME))

        setHasOptionsMenu(true)
        queryString = savedInstanceState?.getString(STATE_QUERY_STRING) ?: ""
    }

    override fun onDestroy() {
        super.onDestroy()

        realm.close()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(STATE_QUERY_STRING, queryString)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.fragment_all_groups, menu)
        (menu?.findItem(R.id.search)?.actionView as SearchView?)?.apply {
            if (!queryString.isEmpty()) {
                // SearchViewを表示させてからクエリをリストア
                onActionViewExpanded()
                setQuery(queryString, false)
            }
            setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    queryString = newText ?: ""

                    (list.adapter as AllGroupsAdapter).swapResults(realm.opGetAllGroupsOrderedByName(queryString))

                    return false
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }
            })

        }
    }

    @Suppress("HasPlatformType")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_group_list, container, false)
        list = view.findViewById(R.id.list)

        val allGroups = realm.opGetAllGroupsOrderedByName(queryString)

        val context = context!!
        list.layoutManager = LinearLayoutManager(context)
        list.addItemDecoration(ItemDividerDecoration(context))
        list.adapter = AllGroupsAdapter(context, allGroups)
        list.setHasFixedSize(true)

        return view
    }

    internal class AllGroupsHeaderVH(headerView: View) : SectionedViewHolder(headerView) {
        val groupName: TextView = itemView.findViewById(android.R.id.text1)
    }

    inner class AllGroupsItemVH(itemView: View) : SectionedViewHolder(itemView) {
        val artifactName: TextView = itemView.findViewById(android.R.id.text1)
        val versions: TextView = itemView.findViewById(android.R.id.text2)
    }

    inner class AllGroupsAdapter(context: Context, private var allGroups: RealmResults<Group>)
        : SectionedRecyclerViewAdapter<SectionedViewHolder>() {

        private val headerColor: Int

        private val groupNameToArtifacts: MutableMap<String/*group name*/, RealmResults<Artifact>> = mutableMapOf()

        init {
            val attrs = context.obtainStyledAttributes(ATTRS)
            headerColor = attrs.getColor(0, Color.WHITE)
            attrs.recycle()

            onListUpdated()
        }

        private fun onListUpdated() {
            rebuildSectionMap(allGroups)

            allGroups.addChangeListener(RealmChangeListener<RealmResults<Group>> {
                rebuildSectionMap(allGroups)
                notifyDataSetChanged()
            })
            notifyDataSetChanged()
        }

        fun swapResults(results: RealmResults<Group>) {
            allGroups.removeAllChangeListeners()
            allGroups = results

            onListUpdated()
        }

        private fun rebuildSectionMap(allGroups: RealmResults<Group>) {
            groupNameToArtifacts.values.forEach {
                it.removeAllChangeListeners()
            }
            groupNameToArtifacts.clear()

            var sectionIndex = 0
            allGroups.forEach {
                val artifacts: RealmResults<Artifact> = it.artifacts!!
                val currentSectionIndex = sectionIndex
                artifacts.addChangeListener(RealmChangeListener<RealmResults<Artifact>> {
                    notifySectionChanged(currentSectionIndex)
                })
                groupNameToArtifacts[it.groupName!!] = artifacts
                sectionIndex++
            }
        }

        override fun getSectionCount() = allGroups.size

        override fun getItemCount(section: Int) = allGroups[section]?.artifacts?.size ?: 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionedViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                VIEW_TYPE_HEADER -> {
                    val view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
                    view.setBackgroundColor(headerColor)
                    AllGroupsHeaderVH(view)
                }
                VIEW_TYPE_FOOTER -> throw RuntimeException("we don't use footer")
                else /* normal item */ -> {
                    val view = inflater.inflate(android.R.layout.simple_list_item_2, parent, false)
                    AllGroupsItemVH(view)
                }
            }
        }

        override fun onBindHeaderViewHolder(holder: SectionedViewHolder, section: Int, expanded: Boolean) {
            val headerHolder = holder as AllGroupsHeaderVH
            headerHolder.groupName.text = allGroups[section]?.groupName
        }

        override fun onBindViewHolder(holder: SectionedViewHolder, section: Int, relativePosition: Int, absolutePosition: Int) {
            val itemHolder = holder as AllGroupsItemVH
            val artifact: Artifact = allGroups[section]!!.artifacts!![relativePosition]!!
            itemHolder.artifactName.text = artifact.artifactName
            itemHolder.versions.text = artifact.versions

            val artifactId = Artifact.toId(artifact.groupName, artifact.artifactName)
            itemHolder.itemView.setOnClickListener {
                realm.executeTransaction { r ->
                    val favorited = r.opToggleFavorite(artifactId)
                    Toast.makeText(itemHolder.itemView.context,
                            if (favorited) "favorited" else "unfavorited",
                            Toast.LENGTH_SHORT).show()
                }
            }
        }

        override fun onBindFooterViewHolder(holder: SectionedViewHolder, section: Int) {
            throw RuntimeException("we don't use footer")
        }
    }
}
