package com.mobikul.bagisto.components

import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Bridge component for switching app theme (light/dark mode).
 * 
 * This component enables the web layer to control the Android app's
 * theme using AppCompat's night mode functionality.
 * 
 * @property name The bridge component name used in web calls
 * @property bridgeDelegate Delegate for handling bridge communication
 * 
 * @see BridgeComponent
 * @see AppCompatDelegate
 * 
 * Usage from JavaScript:
 * ```javascript
 * // Switch to light theme
 * window.BagistoNative.theme.update({ theme: 'light' });
 * 
 * // Switch to dark theme
 * window.BagistoNative.theme.update({ theme: 'dark' });
 * 
 * // Follow system theme
 * window.BagistoNative.theme.update({ theme: null });
 * ```
 * 
 * @constructor
 * @param name Component identifier for the bridge
 * @param bridgeDelegate Bridge delegate for message handling
 */
class ThemeComponent(
    name: String,
    bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {

    /**
     * Handle incoming messages from the web layer.
     * 
     * Processes theme update events and applies the selected theme.
     * 
     * @param message The incoming message from web layer
     * 
     * @see Message
     * @see AppCompatDelegate
     */
    override fun onReceive(message: Message) {
        val data = message.data<MessageData>() ?: return
        when (data.theme) {
            Theme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Theme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            null -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    /**
     * Data class for theme update configuration.
     * 
     * @property theme The theme to switch to: LIGHT, DARK, or null (system)
     */
    @Serializable
    private data class MessageData(
        val theme: Theme?
    )

    /**
     * Enum representing available theme options.
     * 
     * @property LIGHT Light theme mode
     * @property DARK Dark theme mode
     */
    @Serializable
    private enum class Theme {
        @SerialName("light")
        LIGHT,

        @SerialName("dark")
        DARK
    }
}
