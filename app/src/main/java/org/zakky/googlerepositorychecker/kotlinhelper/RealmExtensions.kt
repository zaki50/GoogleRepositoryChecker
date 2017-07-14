@file:JvmName("RealmUtils")

package org.zakky.googlerepositorychecker.kotlinhelper

import io.realm.*
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1

fun <T : RealmModel> Realm.createObject(klass: KClass<T>): T {
    return this.createObject(klass.java)
}

fun <T : RealmModel> Realm.createObject(klass: KClass<T>, primaryKeyValue: Any?): T {
    return this.createObject(klass.java, primaryKeyValue)
}

fun Realm.delete(klass: KClass<out RealmModel>): Unit {
    return this.delete(klass.java)
}

fun <T : RealmModel> Realm.where(klass: KClass<T>): RealmQuery<T> {
    return this.where(klass.java)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, Boolean?>,
                                           value: Boolean): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, Long?>,
                                           value: Long): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, Int?>,
                                           value: Int): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, Short?>,
                                           value: Short): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, Byte?>,
                                           value: Byte): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, Date?>,
                                           value: Date): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, ByteArray?>,
                                           value: ByteArray): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, Double?>,
                                           value: Double): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, Float?>,
                                           value: Float): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, String?>,
                                           value: String, case: Case = Case.SENSITIVE): RealmQuery<T> {
    return this.equalTo(property.name, value, case)
}

fun <T : RealmModel> RealmQuery<T>.findAllSorted(property: KMutableProperty1<T, out Any?>,
                                                 sortOrder: Sort = Sort.ASCENDING): RealmResults<T> {
    return this.findAllSorted(property.name, sortOrder)
}

fun <T : RealmModel> RealmQuery<T>.findAllSortedAsync(property: KMutableProperty1<T, out Any?>,
                                                 sortOrder: Sort = Sort.ASCENDING): RealmResults<T> {
    return this.findAllSortedAsync(property.name, sortOrder)
}

