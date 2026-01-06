package com.masilotti.demo.utils

import android.content.Context
import android.content.SharedPreferences

object AppSharedPreference {
    private const val PREF_NAME = "AppSharedPreference"
    private const val IS_DARK = "isDark"
    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        if (!::sharedPreferences.isInitialized) {
            sharedPreferences = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    fun setDisplayTheme(isDarkMode: Boolean) {
        sharedPreferences.edit().putBoolean(IS_DARK, isDarkMode).apply()
    }

    fun getDisplayTheme(): Boolean {
        return sharedPreferences.getBoolean(IS_DARK, false)
    }
}
