package com.mobikul.bagisto.utils

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

/**
 * Singleton for managing theme state across the application.
 * 
 * This holder provides a centralized way to track and broadcast
 * theme changes throughout the app.
 * 
 * @property currentTheme The current theme mode (light/dark/system)
 * 
 * @see ThemeModeComponent
 * @see ThemeComponent
 * 
 * Usage:
 * ```kotlin
 * ThemeStateHolder.currentTheme = ThemeMode.DARK
 * ```
 */
object ThemeStateHolder {
    private val _isDarkTheme = mutableStateOf(false)
    val isDarkTheme: State<Boolean> = _isDarkTheme

    /**
     * Update the current theme state.
     * 
     * Changes the theme and triggers recomposition in Compose
     * components observing this state.
     * 
     * @param isDark true for dark theme, false for light theme
     */
    fun updateTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }
}
