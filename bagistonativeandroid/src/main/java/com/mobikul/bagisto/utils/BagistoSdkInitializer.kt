package com.mobikul.bagisto.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.startup.Initializer

class BagistoSdkInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        // Initialize Shared Preferences inside the library
        AppSharedPreference.init(context)
        
        // Read theme settings and apply to Android's default night mode handler
        val isDark = AppSharedPreference.getDisplayTheme()
        ThemeStateHolder.updateTheme(isDark) // Initialize theme state for Compose
        
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
