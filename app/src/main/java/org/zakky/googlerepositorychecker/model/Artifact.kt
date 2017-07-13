package org.zakky.googlerepositorychecker.model

import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import io.realm.annotations.Required

@RealmClass
open class Artifact(
        @Required
        var groupName: String? = null,
        @Required
        var artifactName: String? = null,
        @Required
        var versions: String? = null
) : RealmModel {
    val versionList: List<String>
        get() = versions!!.split(",")

    override fun toString(): String {
        val stringBuilder = StringBuilder("Artifact = ")
        stringBuilder.append(if (RealmObject.isManaged(this)) "managed" else "unmanaged")
        stringBuilder.append("{groupName:")
        stringBuilder.append(groupName)
        stringBuilder.append("},{artifactName:")
        stringBuilder.append(artifactName)
        stringBuilder.append("},{versions:")
        stringBuilder.append(versions)
        stringBuilder.append("}")
        return stringBuilder.toString()
    }
}