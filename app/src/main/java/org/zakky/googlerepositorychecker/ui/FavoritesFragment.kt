package org.zakky.googlerepositorychecker.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmRecyclerViewAdapter
import org.zakky.googlerepositorychecker.MyApplication
import org.zakky.googlerepositorychecker.R
import org.zakky.googlerepositorychecker.model.Artifact
import org.zakky.googlerepositorychecker.realm.opGetFavoriteArtifacts
import org.zakky.googlerepositorychecker.ui.recyclerview.ItemDividerDecoration
import toothpick.Toothpick
import javax.inject.Inject

class FavoritesFragment : Fragment() {
    companion object {
        fun newInstance(): FavoritesFragment {
            return FavoritesFragment()
        }
    }

    @Inject
    lateinit var realm: Realm

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

        val favoriteArtifacts = realm.opGetFavoriteArtifacts()

        val list: RecyclerView = view.findViewById(R.id.list)
        list.layoutManager = LinearLayoutManager(context)
        list.addItemDecoration(ItemDividerDecoration(context))
        list.adapter = FavoritesAdapter(favoriteArtifacts)
        list.setHasFixedSize(true)

        return view
    }
}

internal class FavoritesItemVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val artifactId: TextView = itemView.findViewById(android.R.id.text1)
    val versions: TextView = itemView.findViewById(android.R.id.text2)
}

internal class FavoritesAdapter(favorites: RealmList<Artifact>)
    : RealmRecyclerViewAdapter<Artifact, FavoritesItemVH>(favorites, true) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesItemVH {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(android.R.layout.simple_list_item_2, parent, false)
        return FavoritesItemVH(view)
    }

    override fun onBindViewHolder(holder: FavoritesItemVH, position: Int) {
        val item = getItem(position)

        holder.artifactId.text = item?.id
        holder.versions.text = item?.versions
    }
}
