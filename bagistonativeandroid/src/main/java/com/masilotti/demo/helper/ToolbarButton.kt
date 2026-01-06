package com.masilotti.demo.helper

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import dev.hotwire.navigation.fragments.HotwireFragment

@Composable
fun ToolbarButton(
    imageName: String,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val icon = when (imageName.lowercase()) {
        "sunny" -> Icons.Default.WbSunny
        "dark_mode" -> Icons.Default.DarkMode
        "image_search" -> Icons.Default.ImageSearch
        "qr_code_scanner" -> Icons.Default.QrCodeScanner
        "share" -> Icons.Default.Share
        "location" -> Icons.Default.MyLocation
        "logout" -> Icons.AutoMirrored.Filled.Logout
        "download" -> Icons.Default.Download
        else -> Icons.Default.Help // fallback icon
    }

    IconButton(
        onClick = onClick,
        modifier = Modifier.padding(0.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = imageName,
            tint = if (isDark) Color.White else Color.Black
        )
    }
}