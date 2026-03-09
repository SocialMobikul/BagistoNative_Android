package com.mobikul.bagisto.components

import android.util.Log
import androidx.compose.ui.platform.ComposeView
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.fragments.HotwireFragment

/**
 * Bridge component for navigation bar button management.
 * 
 * This component provides low-level control over navigation bar
 * buttons in the toolbar.
 * 
 * @property name The bridge component name used in web calls
 * @property bridgeDelegate Delegate for handling bridge communication
 * 
 * @see BridgeComponent
 * 
 * @constructor
 * @param name Component identifier for the bridge
 * @param bridgeDelegate Bridge delegate for message handling
 */
class NavBarButtonComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val buttonId = 1
    private val TAG = "NavBarButtonComponent"
    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

    /**
     * Handle incoming bridge messages.
     * 
     * Processes messages from the web layer for navigation bar
     * button management. Currently logs messages for debugging.
     * 
     * @param message The incoming bridge message containing event and data
     */
    override fun onReceive(message: Message) {
        Log.d(TAG, "NavBarButtonComponent message -> ${message}")
    }


    /**
     * Remove navigation bar button from toolbar.
     * 
     * Finds the ComposeView button by ID and removes it from
     * the toolbar.
     */
    private fun removeButton() {
        val toolbar = fragment.toolbarForNavigation()
        val button = toolbar?.findViewById<ComposeView>(buttonId)
        toolbar?.removeView(button)
    }

}
