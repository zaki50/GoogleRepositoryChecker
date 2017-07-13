package org.zakky.googlerepositorychecker

import org.zakky.googlerepositorychecker.toothpick.FactoryRegistry
import org.zakky.googlerepositorychecker.toothpick.MemberInjectorRegistry
import toothpick.Toothpick
import toothpick.configuration.Configuration
import toothpick.registries.FactoryRegistryLocator
import toothpick.registries.MemberInjectorRegistryLocator

class DebugApplication : MyApplication() {
    override fun setToothpickConfiguration() {
        val configForDevelop = Configuration.forDevelopment()
                .disableReflection()
                .preventMultipleRootScopes()
        Toothpick.setConfiguration(configForDevelop)
        FactoryRegistryLocator.setRootRegistry(FactoryRegistry())
        MemberInjectorRegistryLocator.setRootRegistry(MemberInjectorRegistry());
    }
}
