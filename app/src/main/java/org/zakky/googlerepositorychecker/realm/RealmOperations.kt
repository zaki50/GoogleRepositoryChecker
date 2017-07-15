@file:JvmName("RealmOperations")

package org.zakky.googlerepositorychecker.realm

import io.realm.Realm
import io.realm.RealmObject
import org.zakky.googlerepositorychecker.model.Artifact
import org.zakky.googlerepositorychecker.model.Favorite
import org.zakky.googlerepositorychecker.model.FavoritesContainer
import org.zakky.googlerepositorychecker.model.Group

fun Realm.opCreateInitialDataIfNeeded() {
    if (where(FavoritesContainer::class).findFirst() != null) {
        return
    }
    createObject(FavoritesContainer::class)
}

fun Realm.opContainsAnyArtifacts(): Boolean {
    val allArtifacts = where(Artifact::class).findAll()
    return !allArtifacts.isEmpty()
}

fun Realm.opGetAllGroupsOrderedByName() = where(Group::class).findAllSorted(Group::groupName)

fun Realm.opGetFavoriteArtifacts() = where(FavoritesContainer::class)
        .findFirst()!!.favorites

fun Realm.opDeleteAllGroupsAndArtifacts() {
    delete(Group::class)
    delete(Artifact::class)
}

fun Realm.opImportArtifacts(unmanagedArtifacts: List<Artifact>) {
    if (unmanagedArtifacts.isEmpty()) {
        return
    }
    val groupName = unmanagedArtifacts[0].groupName
    var group = where(Group::class).equalTo(Group::groupName, groupName).findFirst()
    if (group == null) {
        group = createObject(Group::class, groupName)
    }
    unmanagedArtifacts.forEach { it.group = group }
    insertOrUpdate(unmanagedArtifacts)
    unmanagedArtifacts.forEach { it.group = null }
}

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
