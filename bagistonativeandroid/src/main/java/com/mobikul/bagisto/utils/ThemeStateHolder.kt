package com.mobikul.bagisto.utils

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

object ThemeStateHolder {
    private val _isDarkTheme = mutableStateOf(false)
    val isDarkTheme: State<Boolean> = _isDarkTheme

    fun updateTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }
}
