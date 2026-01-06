package com.masilotti.demo.components

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.json.JSONObject

class ThemeModeComponent(
    name: String,
    bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val TAG = "ThemeModeComponent"

    override fun onReceive(message: Message) {
        Log.d(TAG, "add button message 1 -> ${message}")
        val theme = JSONObject(message.jsonData).getString("theme")
        Log.d(TAG, "theme is -> ${theme}")
        when (theme) {
           "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            null -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    @Serializable
    private data class MessageData(
        val theme: Theme?
    )

    @Serializable
    private enum class Theme {
        @SerialName("light")
        LIGHT,

        @SerialName("dark")
        DARK
    }
}