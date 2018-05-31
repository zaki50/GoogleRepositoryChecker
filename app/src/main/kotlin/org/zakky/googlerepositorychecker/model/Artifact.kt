package org.zakky.googlerepositorychecker.model

import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required

@RealmClass
open class Artifact(
        @Required
        var groupName: String = "",
        @Required
        var artifactName: String = "",
        @Required
        var versions: String = "",
        var group: Group? = null,
        @PrimaryKey
        @Required
        var id: String = Artifact.toId(groupName, artifactName),
        var showInAll: Boolean = true
) : RealmModel {
    companion object {
        fun toId(groupName: String, artifactName: String): String {
            return "$groupName:$artifactName"
        }
    }

    val versionList: List<String>
        get() = versions.split(",")

    override fun toString(): String {
        val stringBuilder = StringBuilder("Artifact = ")
        stringBuilder.append(if (RealmObject.isManaged(this)) "managed" else "unmanaged")
        stringBuilder.append("[{groupName:")
        stringBuilder.append(groupName)
        stringBuilder.append("},{artifactName:")
        stringBuilder.append(artifactName)
        stringBuilder.append("},{versions:")
        stringBuilder.append(versions)
        stringBuilder.append("}]")
        return stringBuilder.toString()
    }
}