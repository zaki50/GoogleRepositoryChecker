package org.zakky.googlerepositorychecker

import android.annotation.SuppressLint
import android.app.Application
import io.realm.Realm
import org.zakky.googlerepositorychecker.kotlinhelper.createObject
import org.zakky.googlerepositorychecker.kotlinhelper.where
import org.zakky.googlerepositorychecker.model.FavoritesContainer
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
        val APP_SCOPE_NAME = "AppScope"
    }

    override fun onCreate() {
        super.onCreate()

        setupRealm()
        setupToothpick()
        setupInitialData()
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

    open protected fun setToothpickConfiguration() {
        val config = Configuration.forProduction()
                .disableReflection()

        Toothpick.setConfiguration(config)
        FactoryRegistryLocator.setRootRegistry(FactoryRegistry())
        MemberInjectorRegistryLocator.setRootRegistry(MemberInjectorRegistry());
    }

    private fun setupInitialData() {
        Toothpick.openScope(APP_SCOPE_NAME).getInstance(Realm::class.java).use {
            if (it.where(FavoritesContainer::class).findFirst() == null) {
                it.executeTransaction {
                    it.createObject(FavoritesContainer::class)
                }
            }
        }
    }
}
