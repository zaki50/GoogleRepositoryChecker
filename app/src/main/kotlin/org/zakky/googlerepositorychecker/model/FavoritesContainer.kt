package org.zakky.googlerepositorychecker.model

import io.realm.RealmList
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass
open class FavoritesContainer(
        var favorites: RealmList<Artifact> = RealmList()
) : RealmModel {
    override fun toString(): String {
        val stringBuilder = StringBuilder("FavoritesContainer = ")
        stringBuilder.append(if (RealmObject.isManaged(this)) "managed" else "unmanaged")
        stringBuilder.append("[{favorites:")
        stringBuilder.append(favorites)
        stringBuilder.append("}]")
        return stringBuilder.toString()
    }
}