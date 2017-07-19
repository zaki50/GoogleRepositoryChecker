@file:JvmName("RealmUtils")

package org.zakky.googlerepositorychecker.realm

import io.realm.*
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1

fun RealmModel.deleteFromRealm() {
    RealmObject.deleteFromRealm(this)
}

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

fun <T : RealmModel> RealmQuery<T>.group(body: RealmQuery<T>.() -> Unit) {
    beginGroup()
    body()
    endGroup()
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, out Boolean?>,
                                           value: Boolean): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, out Long?>,
                                           value: Long): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, out Int?>,
                                           value: Int): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, out Short?>,
                                           value: Short): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, out Byte?>,
                                           value: Byte): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, out Date?>,
                                           value: Date): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, out ByteArray?>,
                                           value: ByteArray): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, out Double?>,
                                           value: Double): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, out Float?>,
                                           value: Float): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, out String?>,
                                           value: String, case: Case = Case.SENSITIVE): RealmQuery<T> {
    return this.equalTo(property.name, value, case)
}

fun <T : RealmModel> RealmQuery<T>.`in`(property: KMutableProperty1<T, out Boolean?>,
                                           value: Array<Boolean?>): RealmQuery<T> {
    return this.`in`(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.`in`(property: KMutableProperty1<T, out Long?>,
                                           value: Array<Long?>): RealmQuery<T> {
    return this.`in`(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.`in`(property: KMutableProperty1<T, out Int?>,
                                           value: Array<Int?>): RealmQuery<T> {
    return this.`in`(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.`in`(property: KMutableProperty1<T, out Short?>,
                                           value: Array<Short?>): RealmQuery<T> {
    return this.`in`(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.`in`(property: KMutableProperty1<T, out Byte?>,
                                           value: Array<Byte?>): RealmQuery<T> {
    return this.`in`(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.`in`(property: KMutableProperty1<T, out Date?>,
                                           value: Array<Date?>): RealmQuery<T> {
    return this.`in`(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.`in`(property: KMutableProperty1<T, out Double?>,
                                           value: Array<Double?>): RealmQuery<T> {
    return this.`in`(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.`in`(property: KMutableProperty1<T, out Float?>,
                                           value: Array<Float?>): RealmQuery<T> {
    return this.`in`(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.`in`(property: KMutableProperty1<T, out String?>,
                                           value: Array<String?>, casing: Case = Case.SENSITIVE): RealmQuery<T> {
    return this.`in`(property.name, value, casing)
}

fun <T : RealmModel> RealmQuery<T>.findAllSorted(property: KMutableProperty1<T, out Any?>,
                                                 sortOrder: Sort = Sort.ASCENDING): RealmResults<T> {
    return this.findAllSorted(property.name, sortOrder)
}

fun <T : RealmModel> RealmQuery<T>.findAllSortedAsync(property: KMutableProperty1<T, out Any?>,
                                                      sortOrder: Sort = Sort.ASCENDING): RealmResults<T> {
    return this.findAllSortedAsync(property.name, sortOrder)
}
