package com.example.bagisto_native_example

import android.app.Application
import androidx.multidex.MultiDexApplication
import dev.hotwire.core.bridge.KotlinXJsonConverter
import dev.hotwire.core.config.Hotwire

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
    }
}
