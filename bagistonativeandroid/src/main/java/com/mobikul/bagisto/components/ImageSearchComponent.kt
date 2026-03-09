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
 * Bridge component for image-based product search.
 * 
 * This component uses Google ML Kit Image Labeling to identify
 * products in images and enables reverse image search functionality.
 * 
 * Features:
 * - Camera capture for product images
 * - Gallery image selection
 * - ML Kit image labeling for product identification
 * - Search results via bridge event
 * 
 * @property name The bridge component name used in web calls
 * @property bridgeDelegate Delegate for handling bridge communication
 * @property activity The host activity for camera/gallery operations
 * 
 * @see BridgeComponent
 * @see ImageLabelAnalyzer
 * 
 * @constructor
 * @param name Component identifier for the bridge
 * @param bridgeDelegate Bridge delegate for message handling
 * @param activity Host activity for camera/gallery access
 */
class ImageSearchComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val TAG = "ImageSearchComponent"
    private val buttonId = 1
    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

    /**
     * Handle incoming messages from the web layer.
     * 
     * Processes image search related events from web layer.
     * 
     * @param message The incoming message from web layer
     * 
     * @see Message
     */
    override fun onReceive(message: Message) {
        Log.d(TAG,"dynamic message -> ${message}")

    }

    /**
     * Data class for image search configuration.
     * 
     * @property title Title for the search (default: "test title")
     * @property imageName Optional image name for the button icon
     */
    @Serializable
    data class MessageData(
        val title: String = "test title",
        @SerialName("androidImage") val imageName: String?
    )
}
