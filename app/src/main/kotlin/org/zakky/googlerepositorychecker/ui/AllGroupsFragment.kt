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
import org.zakky.googlerepositorychecker.realm.extensions.callTransaction
import org.zakky.googlerepositorychecker.realm.opGetAllArtifacts
import org.zakky.googlerepositorychecker.realm.opToggleFavorite
import org.zakky.googlerepositorychecker.ui.recyclerview.ItemDividerDecoration
import toothpick.Toothpick
import javax.inject.Inject
import kotlin.Comparator

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

                    realm.callTransaction {
                        opGetAllArtifacts(false).forEach {
                            it.showInAll = it.groupName.contains(queryString) || it.artifactName.contains(queryString)
                        }
                    }
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

        val allGroups = realm.opGetAllArtifacts(true)

        val context = context!!
        list.layoutManager = LinearLayoutManager(context)
        list.addItemDecoration(ItemDividerDecoration(context))
        list.adapter = AllArtifactsAdapter(context, allGroups)
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

    inner class AllArtifactsAdapter(context: Context, private val allArtifacts: RealmResults<Artifact>)
        : SectionedRecyclerViewAdapter<SectionedViewHolder>() {

        private val headerColor: Int

        private val groupNames: MutableList<String> = mutableListOf()
        private val groupNameToArtifacts: MutableMap<String/*group name*/, MutableList<Artifact>> = mutableMapOf()

        init {
            val attrs = context.obtainStyledAttributes(ATTRS)
            headerColor = attrs.getColor(0, Color.WHITE)
            attrs.recycle()

            rebuildSectionMap(allArtifacts)

            allArtifacts.addChangeListener(RealmChangeListener<RealmResults<Artifact>> {
                rebuildSectionMap(allArtifacts)
                notifyDataSetChanged()
            })
            notifyDataSetChanged()
        }

        private fun rebuildSectionMap(artifacts: RealmResults<Artifact>) {
            groupNames.clear()
            groupNameToArtifacts.clear()

            artifacts.forEach { artifact ->
                val groupName = artifact.groupName

                val artifactsForGroup = groupNameToArtifacts[groupName] ?: mutableListOf<Artifact>().also {
                    groupNames.add(groupName)
                    groupNameToArtifacts[groupName] = it
                }

                artifactsForGroup.add(artifact)
            }
            groupNames.sort()

            groupNameToArtifacts.values.forEach {
                it.sortWith(Comparator { o1, o2 ->
                    if (o1 == null) {
                        return@Comparator if (o2 == null) 0 else -1
                    }
                    if (o2 == null) {
                        return@Comparator 1
                    }
                    return@Comparator o1.artifactName.compareTo(o2.artifactName)
                })
            }
        }

        override fun getSectionCount() = groupNameToArtifacts.size

        override fun getItemCount(section: Int) = groupNameToArtifacts[groupNames[section]]?.size ?: 0

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
            headerHolder.groupName.text = groupNames[section]
        }

        override fun onBindViewHolder(holder: SectionedViewHolder, section: Int, relativePosition: Int, absolutePosition: Int) {
            val itemHolder = holder as AllGroupsItemVH
            val artifact = groupNameToArtifacts[groupNames[section]]!![relativePosition]

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
