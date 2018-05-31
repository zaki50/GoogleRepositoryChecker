package org.zakky.googlerepositorychecker

import android.annotation.SuppressLint
import android.app.Application
import io.realm.Realm
import org.zakky.googlerepositorychecker.realm.opCreateInitialDataIfNeeded
import org.zakky.googlerepositorychecker.realm.opResetViewState
import org.zakky.googlerepositorychecker.toothpick.ApplicationModule
import org.zakky.googlerepositorychecker.toothpick.FactoryRegistry
import org.zakky.googlerepositorychecker.toothpick.MemberInjectorRegistry
import toothpick.Toothpick
import toothpick.configuration.Configuration
import toothpick.registries.FactoryRegistryLocator
import toothpick.registries.MemberInjectorRegistryLocator

@SuppressLint("Registered")
open class MyApplication : Application() {

    companion object {
        const val APP_SCOPE_NAME = "AppScope"
    }

    override fun onCreate() {
        super.onCreate()

        setupRealm()
        setupToothpick()
        prepareRealmData()
    }

    override fun onTerminate() {
        super.onTerminate()

        Toothpick.closeScope(APP_SCOPE_NAME)
    }

    private fun setupRealm() {
        Realm.init(this)
    }

    private fun setupToothpick() {
        setToothpickConfiguration()

        val scope = Toothpick.openScope(APP_SCOPE_NAME)
        scope.installModules(ApplicationModule(this))
    }

    protected open fun setToothpickConfiguration() {
        val config = Configuration.forProduction()
                .disableReflection()

        Toothpick.setConfiguration(config)
        FactoryRegistryLocator.setRootRegistry(FactoryRegistry())
        MemberInjectorRegistryLocator.setRootRegistry(MemberInjectorRegistry())
    }

    private fun prepareRealmData() {
        Toothpick.openScope(APP_SCOPE_NAME).getInstance(Realm::class.java).use { realm ->
            realm.executeTransaction {
                it.opCreateInitialDataIfNeeded()
                it.opResetViewState()
            }
        }
    }
}
