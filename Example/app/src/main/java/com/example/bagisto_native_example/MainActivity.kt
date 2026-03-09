package com.example.bagisto_native_example

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import dev.hotwire.navigation.activities.HotwireActivity
import dev.hotwire.navigation.navigator.NavigatorConfiguration
import dev.hotwire.navigation.util.applyDefaultImeWindowInsets

/**
 * Main Activity demonstrating BagistoNative SDK integration.
 * 
 * This activity hosts the Hotwire Turbo web navigation that displays
 * the Bagisto storefront. 
 * 
 * @see HotwireActivity
 * @see NavigatorConfiguration
 */
class MainActivity : HotwireActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.main_nav_host).applyDefaultImeWindowInsets()
    }

    override fun navigatorConfigurations() = listOf(
        NavigatorConfiguration(
            name = "main",
            startLocation = "https://bagisto-native-commerce.vercel.app/",
            navigatorHostId = R.id.main_nav_host
        )
    )
}
