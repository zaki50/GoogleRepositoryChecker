@file:JvmName("RealmOperations")

package org.zakky.googlerepositorychecker.realm

import io.realm.Realm
import io.realm.RealmObject
import org.zakky.googlerepositorychecker.model.Artifact
import org.zakky.googlerepositorychecker.model.Favorite
import org.zakky.googlerepositorychecker.model.FavoritesContainer

fun Realm.opToggleFavorite(artifactId: String): Boolean {
    val existingFavorite = where(Favorite::class)
            .equalTo(Favorite::artifactId, artifactId).findFirst()
    val artifact = where(Artifact::class)
            .equalTo(Artifact::id, artifactId).findFirst()
    if (artifact == null) {
        // artifact does not exist. Remove `Favorite` and return
        existingFavorite?.let {
            RealmObject.deleteFromRealm(it)
        }
        return false
    }

    val container = where(FavoritesContainer::class).findFirst()!!
    return if (existingFavorite == null) {
        createObject(Favorite::class, artifactId)
        container.favorites.add(0, artifact)
        true
    } else {
        existingFavorite.deleteFromRealm()
        container.favorites.remove(artifact)
        false
    }
}

