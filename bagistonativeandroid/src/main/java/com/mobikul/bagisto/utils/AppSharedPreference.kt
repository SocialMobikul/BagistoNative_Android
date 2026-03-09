package com.mobikul.bagisto.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Singleton object for managing app SharedPreferences.
 * 
 * This object provides a centralized way to store and retrieve
 * app-wide preferences including theme settings.
 * 
 * @property PREF_NAME Name of the shared preferences file
 * @property IS_DARK Key for dark mode preference
 * 
 * @see android.content.SharedPreferences
 * 
 * Usage:
 * ```kotlin
 * // Initialize once in Application class
 * AppSharedPreference.init(context)
 * 
 * // Get theme preference
 * val isDark = AppSharedPreference.getDisplayTheme()
 * 
 * // Set theme preference
 * AppSharedPreference.setDisplayTheme(true)
 * ```
 */
object AppSharedPreference {
    private const val PREF_NAME = "AppSharedPreference"
    private const val IS_DARK = "isDark"
    private lateinit var sharedPreferences: SharedPreferences

    /**
     * Initialize SharedPreferences.
     * 
     * Must be called once before using other methods.
     * Uses application context to avoid memory leaks.
     * 
     * @param context Application context for initialization
     */
    fun init(context: Context) {
        if (!::sharedPreferences.isInitialized) {
            sharedPreferences = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * Save theme preference.
     * 
     * Persists the user's theme choice (light/dark mode).
     * 
     * @param isDarkMode true for dark mode, false for light mode
     */
    fun setDisplayTheme(isDarkMode: Boolean) {
        sharedPreferences.edit().putBoolean(IS_DARK, isDarkMode).apply()
    }

    /**
     * Get saved theme preference.
     * 
     * @return true if dark mode is enabled, false otherwise. Defaults to light.
     */
    fun getDisplayTheme(): Boolean {
        return sharedPreferences.getBoolean(IS_DARK, false)
    }
}
