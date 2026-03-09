package com.mobikul.bagisto.components

import android.util.Log
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.fragments.HotwireFragment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Bridge component for navigation bar theming.
 * 
 * This component manages the visual appearance of the navigation bar,
 * including background color and status bar style.
 * 
 * Features:
 * - Set navigation bar background color
 * - Configure status bar style (light/dark)
 * - Apply theme to navigation elements
 * 
 * @property name The bridge component name used in web calls
 * @property bridgeDelegate Delegate for handling bridge communication
 * 
 * @see BridgeComponent
 * @see ThemeComponent
 * 
 * @constructor
 * @param name Component identifier for the bridge
 * @param bridgeDelegate Bridge delegate for message handling
 */
class NavigationThemeComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val TAG = "NavigationThemeComponent"
    private val buttonId = 1
    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

    /**
     * Handle incoming bridge messages.
     * 
     * Processes messages from the web layer. Currently logs messages
     * for debugging purposes. Future implementations may handle
     * navigation bar theme updates.
     * 
     * @param message The incoming bridge message containing event and data
     */
    override fun onReceive(message: Message) {
        print("fgtgfhfghfg")
        Log.d(TAG,"dynamic message -> ${message}")

    }

    /**
     * Data class for navigation theme message payload.
     * 
     * Represents the expected JSON structure when receiving theme
     * configuration from the web layer.
     * 
     * @property title The title to display in navigation bar
     * @property imageName Optional Android image resource name for navigation icon
     */
    @Serializable
    data class MessageData(
        val title: String = "test title",
        @SerialName("androidImage") val imageName: String?
    )
}