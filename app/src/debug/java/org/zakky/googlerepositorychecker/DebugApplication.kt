package org.zakky.googlerepositorychecker

import toothpick.Toothpick
import toothpick.configuration.Configuration

class DebugApplication : MyApplication() {
    override fun setToothpickConfiguration() {
        val configForDevelop = Configuration.forDevelopment()
                .preventMultipleRootScopes()
        Toothpick.setConfiguration(configForDevelop)
    }
}
