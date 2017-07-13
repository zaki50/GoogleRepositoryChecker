package org.zakky.googlerepositorychecker.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.internal.RealmObjectProxy
import org.zakky.googlerepositorychecker.MyApplication
import org.zakky.googlerepositorychecker.R
import org.zakky.googlerepositorychecker.model.Artifact
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

        val allGroups = realm.where(Artifact::class.java)
                .distinct(Artifact::groupName.name).sort(Artifact::groupName.name)

        list.layoutManager = LinearLayoutManager(context)
        list.adapter = AllGroupsAdapter(allGroups)

        return view
    }
}

internal class AllGroupsVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val groupName:TextView = itemView.findViewById(android.R.id.text1)
    val artifactNames:TextView = itemView.findViewById(android.R.id.text2)
}

internal class AllGroupsAdapter(collection: OrderedRealmCollection<Artifact>)
    : RealmRecyclerViewAdapter<Artifact, AllGroupsVH>(collection, true) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllGroupsVH {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(android.R.layout.simple_list_item_2, parent, false)
        return AllGroupsVH(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AllGroupsVH, position: Int) {
        val item = getItem(position)

        val groupName = item?.groupName
        if (groupName == null) {
            holder.groupName.text =  "(null)"
            holder.artifactNames.text = ""
            return
        }
        holder.groupName.text = groupName

        val objectProxy = item as RealmObjectProxy
        val realm = objectProxy.`realmGet$proxyState`().`realm$realm` as Realm

        val allArtifacts = realm.where(Artifact::class.java)
                .findAllSorted(Artifact::artifactName.name)

        val builder = StringBuilder()
        val separator = ", "
        allArtifacts.forEach {
            builder.append(it.artifactName).append(separator)
        }
        if (builder.isNotEmpty()) {
            builder.setLength(builder.length - separator.length)
        }
        holder.artifactNames.text = builder.toString()
    }
}