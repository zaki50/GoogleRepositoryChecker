package org.zakky.googlerepositorychecker

import android.annotation.SuppressLint
import android.app.Application
import io.realm.Realm
import org.zakky.googlerepositorychecker.toothpick.ApplicationModule
import toothpick.Toothpick
import toothpick.configuration.Configuration

@SuppressLint("Registered")
open class MyApplication : Application() {

    companion object {
        val APP_SCOPE_NAME = "AppScope"
    }

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)

        setupToothpick()
    }

    private fun setupToothpick() {
        setToothpickConfiguration()

        val scope = Toothpick.openScope(APP_SCOPE_NAME)
        scope.installModules(ApplicationModule(this))
    }

    open protected fun setToothpickConfiguration() {
        Toothpick.setConfiguration(Configuration.forProduction().disableReflection())
    }
}