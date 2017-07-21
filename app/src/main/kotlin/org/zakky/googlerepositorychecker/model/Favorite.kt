package org.zakky.googlerepositorychecker.model

import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required
import java.util.*

@RealmClass
open class Favorite(
        @PrimaryKey
        @Required
        var artifactId: String? = null,
        var createdAt: Long = System.currentTimeMillis()
) : RealmModel {
    override fun toString(): String {
        val stringBuilder = StringBuilder("Favorite = ")
        stringBuilder.append(if (RealmObject.isManaged(this)) "managed" else "unmanaged")
        stringBuilder.append("[{artifactId:")
        stringBuilder.append(artifactId)
        stringBuilder.append("},{createdAt(raw):")
        stringBuilder.append(createdAt)
        stringBuilder.append("},{createdAt(Date):")
        stringBuilder.append(Date(createdAt))
        stringBuilder.append("}]")
        return stringBuilder.toString()
    }
}