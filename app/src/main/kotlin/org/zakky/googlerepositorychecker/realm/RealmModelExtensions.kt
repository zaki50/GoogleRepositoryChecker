@file:JvmName("RealmModelUtils")
@file:Suppress("unused")

package org.zakky.googlerepositorychecker.realm

import io.realm.*
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KMutableProperty1

fun RealmModel.deleteFromRealm() {
    RealmObject.deleteFromRealm(this)
}
