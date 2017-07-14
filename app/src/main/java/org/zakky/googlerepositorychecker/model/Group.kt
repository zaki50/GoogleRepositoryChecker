package org.zakky.googlerepositorychecker.model

import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required

@RealmClass
open class Group(
        @PrimaryKey
        @Required
        var groupName: String? = null,
        @LinkingObjects("group")
        val artifacts: RealmResults<Artifact>? = null
) : RealmModel {
    override fun toString(): String {
        val stringBuilder = StringBuilder("Group = ")
        stringBuilder.append(if (RealmObject.isManaged(this)) "managed" else "unmanaged")
        stringBuilder.append("[{groupName:")
        stringBuilder.append(groupName)
        stringBuilder.append("},{artifact count: ")
        stringBuilder.append(artifacts?.size ?: 0)
        stringBuilder.append("}]")
        return stringBuilder.toString()
    }
}