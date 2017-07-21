@file:JvmName("RealmOperations")

package org.zakky.googlerepositorychecker.realm

import io.realm.Realm
import io.realm.RealmObject
import org.zakky.googlerepositorychecker.model.Artifact
import org.zakky.googlerepositorychecker.model.Favorite
import org.zakky.googlerepositorychecker.model.FavoritesContainer
import org.zakky.googlerepositorychecker.model.Group

fun Realm.opCreateInitialDataIfNeeded() {
    if (where<FavoritesContainer>().findFirst() != null) {
        return
    }
    createObject<FavoritesContainer>()
}

fun Realm.opContainsAnyArtifacts(): Boolean {
    val allArtifacts = where<Artifact>().findAll()
    return !allArtifacts.isEmpty()
}

fun Realm.opGetAllGroupsOrderedByName() = where<Group>().findAllSorted(Group::groupName)

fun Realm.opGetFavoriteArtifacts() = where<FavoritesContainer>()
        .findFirst()!!.favorites

fun Realm.opDeleteAllGroupsAndArtifacts() {
    delete<Group>()
    delete<Artifact>()
}

fun Realm.opImportArtifacts(unmanagedArtifacts: List<Artifact>) {
    if (unmanagedArtifacts.isEmpty()) {
        return
    }
    val groupName = unmanagedArtifacts[0].groupName
    var group = where<Group>().equalTo(Group::groupName, groupName).findFirst()
    if (group == null) {
        group = createObject<Group>(groupName)
    }
    val artifactIds = arrayOfNulls<String>(unmanagedArtifacts.size)
    unmanagedArtifacts.forEachIndexed { index: Int, artifact ->
        artifact.group = group
        artifactIds[index] = Artifact.toId(artifact.groupName, artifact.artifactName)
    }
    insertOrUpdate(unmanagedArtifacts)
    unmanagedArtifacts.forEach { it.group = null }

    where<Favorite>().`in`(Favorite::artifactId, artifactIds)
            .findAll()
}

fun Realm.opToggleFavorite(artifactId: String): Boolean {
    val existingFavorite = where<Favorite>()
            .equalTo(Favorite::artifactId, artifactId).findFirst()
    val artifact = where<Artifact>()
            .equalTo(Artifact::id, artifactId).findFirst()
    if (artifact == null) {
        // artifact does not exist. Remove `Favorite` and return
        existingFavorite?.let {
            RealmObject.deleteFromRealm(it)
        }
        return false
    }

    val container = where<FavoritesContainer>().findFirst()!!
    return if (existingFavorite == null) {
        createObject<Favorite>(artifactId)
        container.favorites.add(0, artifact)
        true
    } else {
        existingFavorite.deleteFromRealm()
        container.favorites.remove(artifact)
        false
    }
}

fun Realm.opFavorite(artifactId: String) {
    val existingFavorite = where<Favorite>()
            .equalTo(Favorite::artifactId, artifactId).findFirst()
    val artifact = where<Artifact>()
            .equalTo(Artifact::id, artifactId).findFirst()
    if (artifact == null) {
        // artifact does not exist. Remove `Favorite` and return
        existingFavorite?.let {
            RealmObject.deleteFromRealm(it)
        }
        return
    }

    val container = where<FavoritesContainer>().findFirst()!!
    if (existingFavorite == null) {
        createObject<Favorite>(artifactId)
        container.favorites.add(0, artifact)
    } else {
        existingFavorite.deleteFromRealm()
        container.favorites.remove(artifact)
    }
}