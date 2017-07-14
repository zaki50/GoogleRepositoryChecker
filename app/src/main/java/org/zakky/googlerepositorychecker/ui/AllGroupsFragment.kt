package org.zakky.googlerepositorychecker.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import org.zakky.googlerepositorychecker.MyApplication
import org.zakky.googlerepositorychecker.R
import org.zakky.googlerepositorychecker.model.Artifact
import org.zakky.googlerepositorychecker.model.Group
import org.zakky.googlerepositorychecker.ui.recyclerview.ItemDividerDecoration
import toothpick.Toothpick
import javax.inject.Inject

class AllGroupsFragment : Fragment() {
    companion object {
        fun newInstance(): AllGroupsFragment {
            return AllGroupsFragment()
        }
    }

    @Inject
    lateinit var realm: Realm

    private lateinit var list: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Toothpick.inject(this, Toothpick.openScope(MyApplication.APP_SCOPE_NAME))
    }

    override fun onDestroy() {
        super.onDestroy()

        realm.close()
    }

    @Suppress("HasPlatformType")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_group_list, container, false)
        list = view.findViewById(R.id.list)

        val allGroups = realm.where(Group::class.java)
                .findAllSorted(Group::groupName.name)

        list.layoutManager = LinearLayoutManager(context)
        list.addItemDecoration(ItemDividerDecoration(context))
        list.adapter = AllGroupsAdapter(context, allGroups)
        list.setHasFixedSize(true)

        return view
    }
}

internal class AllGroupsHeaderVH(headerView: View) : SectionedViewHolder(headerView) {
    val groupName:TextView = itemView.findViewById(android.R.id.text1)
}

internal class AllGroupsItemVH(itemView: View) : SectionedViewHolder(itemView) {
    val artifactNames:TextView = itemView.findViewById(android.R.id.text1)
    val versions:TextView = itemView.findViewById(android.R.id.text2)
}

internal class AllGroupsAdapter(context: Context, private val allGroups: RealmResults<Group>)
    : SectionedRecyclerViewAdapter<SectionedViewHolder>() {

    companion object {
        private val ATTRS = intArrayOf(
                R.attr.colorAccent
        )
    }

    private var headerColor: Int

    private var groupNameToArtifacts: Map<String, Pair<RealmResults<Artifact>, Int/* section index*/>>

    init {
        val attrs = context.obtainStyledAttributes(ATTRS)
        headerColor = attrs.getColor(0, Color.WHITE)
        attrs.recycle()

        groupNameToArtifacts = buildSectionMap(allGroups)

        allGroups.addChangeListener(RealmChangeListener<RealmResults<Group>> {
            groupNameToArtifacts = buildSectionMap(allGroups)
            notifyDataSetChanged()
        })
    }

    private fun buildSectionMap(allGroups: RealmResults<Group>): Map<String, Pair<RealmResults<Artifact>, Int>> {
        val map = HashMap<String, Pair<RealmResults<Artifact>, Int>>(allGroups.size)

        var sectionIndex = 0
        allGroups.forEach {
            val artifacts: RealmResults<Artifact> = it.artifacts!!
            map.put(it.groupName!!, artifacts to sectionIndex)
            sectionIndex++
        }

        return map
    }

    override fun getSectionCount(): Int {
        return allGroups.size
    }

    override fun getItemCount(section: Int): Int {
        return allGroups[section].artifacts?.size ?: 0
    }

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
        headerHolder.groupName.text = allGroups[section].groupName
    }

    override fun onBindViewHolder(holder: SectionedViewHolder, section: Int, relativePosition: Int, absolutePosition: Int) {
        val itemHolder = holder as AllGroupsItemVH
        val artifact: Artifact = allGroups[section]!!.artifacts!![relativePosition]
        itemHolder.artifactNames.text = artifact.artifactName
        itemHolder.versions.text = artifact.versions
    }

    override fun onBindFooterViewHolder(holder: SectionedViewHolder, section: Int) {
        throw RuntimeException("we don't use footer")
    }
}