@file:JvmName("RealmUtils")
@file:Suppress("unused")

package org.zakky.googlerepositorychecker.realm.extensions

import io.realm.*
import java.util.concurrent.atomic.AtomicReference

inline fun <T> Realm.callTransaction(crossinline action: Realm.() -> T): T {
    val ref = AtomicReference<T>()
    executeTransaction {
        ref.set(action(it))
    }
    return ref.get()
}

inline fun <reified T : RealmModel> Realm.createObject(): T {
    return this.createObject(T::class.java)
}

inline fun <reified T : RealmModel> Realm.createObject(primaryKeyValue: Any?): T {
    return this.createObject(T::class.java, primaryKeyValue)
}

inline fun <reified T : RealmModel> Realm.delete(): Unit {
    return this.delete(T::class.java)
}

inline fun <reified T : RealmModel> Realm.where(): RealmQuery<T> {
    return this.where(T::class.java)
}
