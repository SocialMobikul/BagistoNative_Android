package com.mobikul.bagisto.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.startup.Initializer

/**
 * App Startup Initializer for Bagisto Native SDK.
 * 
 * This class is used by Android's App Startup library to initialize
 * the Bagisto SDK before any other components are created. It sets up:
 * - Shared preferences initialization
 * - Theme state management
 * - Default night mode configuration
 * 
 * @see AppSharedPreference
 * @see ThemeStateHolder
 * @see androidx.startup.Initializer
 * 
 * Usage:
 * Add to AndroidManifest.xml:
 * ```xml
 * <provider
 *     android:name="androidx.startup.InitializationProvider"
 *     android:authorities="${applicationId}.androidx-startup"
 *     android:exported="false">
 *     <meta-data
 *         android:name="com.mobikul.bagisto.utils.BagistoSdkInitializer"
 *         android:value="androidx.startup" />
 * </provider>
 * ```
 */
class BagistoSdkInitializer : Initializer<Unit> {
    /**
     * Initialize the Bagisto SDK.
     * 
     * Called by Android App Startup to initialize the SDK before
     * any other components. Sets up shared preferences and theme.
     * 
     * @param context Application context
     */
    override fun create(context: Context) {
        AppSharedPreference.init(context)
        
        val isDark = AppSharedPreference.getDisplayTheme()
        ThemeStateHolder.updateTheme(isDark)
        
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    /**
     * Return empty list - no dependencies required.
     * 
     * @return Empty list of initializer dependencies
     */
    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
