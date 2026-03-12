package com.example.bagisto_native_example

import android.app.Application
import androidx.multidex.MultiDexApplication
import com.mobikul.bagisto.fragments.BagistoWebFragment
import com.mobikul.bagisto.utils.CustomBridgeComponents
import dev.hotwire.core.bridge.KotlinXJsonConverter
import dev.hotwire.core.config.Hotwire
import dev.hotwire.navigation.config.defaultFragmentDestination
import dev.hotwire.navigation.config.registerBridgeComponents
import dev.hotwire.navigation.config.registerFragmentDestinations

/**
 * Example Application class demonstrating Hotwire initialization.
 * 
 * Shows basic Hotwire setup - you can add bridge components as needed.
 * 
 * @see Hotwire
 */
class BagistoExampleApplication : MultiDexApplication() {
    
    override fun onCreate() {
        super.onCreate()
        // Configure Hotwire
        Hotwire.config.jsonConverter = KotlinXJsonConverter()
        Hotwire.config.applicationUserAgentPrefix = "BagistoNativeExample"


        Hotwire.config.debugLoggingEnabled = true
        Hotwire.config.webViewDebuggingEnabled = true

        Hotwire.defaultFragmentDestination = BagistoWebFragment::class

        Hotwire.registerFragmentDestinations(
            BagistoWebFragment::class,
        )

        Hotwire.registerBridgeComponents(
            *CustomBridgeComponents.all
        )

    }
}
