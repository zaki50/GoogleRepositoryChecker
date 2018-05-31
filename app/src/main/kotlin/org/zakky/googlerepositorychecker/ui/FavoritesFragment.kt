package org.zakky.googlerepositorychecker.ui

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmRecyclerViewAdapter
import org.zakky.googlerepositorychecker.MyApplication
import org.zakky.googlerepositorychecker.R
import org.zakky.googlerepositorychecker.model.Artifact
import org.zakky.googlerepositorychecker.model.Favorite
import org.zakky.googlerepositorychecker.realm.*
import org.zakky.googlerepositorychecker.realm.extensions.*
import org.zakky.googlerepositorychecker.ui.recyclerview.ItemDividerDecoration
import toothpick.Toothpick
import javax.inject.Inject

class FavoritesFragment : Fragment() {
    companion object {
        fun newInstance() = FavoritesFragment()
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

        val context = context!!
        val list: RecyclerView = view.findViewById(R.id.list)
        list.layoutManager = LinearLayoutManager(context)
        list.addItemDecoration(ItemDividerDecoration(context))
        list.adapter = FavoritesAdapter(favoriteArtifacts)
        list.setHasFixedSize(true)

        return view
    }

    internal class FavoritesItemVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val artifactId: TextView = itemView.findViewById(android.R.id.text1)
        val versions: TextView = itemView.findViewById(android.R.id.text2)
    }

    internal inner class FavoritesAdapter(favorites: RealmList<Artifact>)
        : RealmRecyclerViewAdapter<Artifact, FavoritesItemVH>(favorites, true) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesItemVH {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_artifact, parent, false)
            return FavoritesItemVH(view)
        }

        override fun onBindViewHolder(holder: FavoritesItemVH, position: Int) {
            val artifact = getItem(position)

            holder.artifactId.text = artifact!!.id
            holder.versions.text = artifact.versions

            val artifactId = Artifact.toId(artifact.groupName, artifact.artifactName)
            val favorite = realm.where<Favorite>().equalTo(Favorite::artifactId, artifactId).findFirst()
            val createdAt = favorite?.createdAt
            holder.itemView.setOnClickListener {
                realm.executeTransaction { r ->
                    r.opUnfavorite(artifactId)
                    Toast.makeText(holder.itemView.context, "unfavorited", Toast.LENGTH_SHORT).show()
                }
                Snackbar.make(view!!, "undo", Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.action_undo)) {
                            realm.executeTransaction {
                                realm.opFavorite(artifactId, createdAt)
                            }
                        }
                        .show()
            }
        }
    }
}
