package org.zakky.googlerepositorychecker.toothpick

import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Provider

class RealmProvider(isReleaseBuild: Boolean) : Provider<Realm> {
    private val config = RealmConfiguration.Builder().apply {
        if (!isReleaseBuild) {
            deleteRealmIfMigrationNeeded()
        }
    }.build()

    override fun get(): Realm {
        return Realm.getInstance(config)
    }
}
