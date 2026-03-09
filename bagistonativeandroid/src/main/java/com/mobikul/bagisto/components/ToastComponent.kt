package com.mobikul.bagisto.components

import android.util.Log
import android.widget.Toast
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import kotlinx.serialization.Serializable

/**
 * Bridge component for displaying native Android toast messages.
 * 
 * This component enables the web layer to display short informational
 * messages using Android's native Toast notification system.
 * 
 * @property name The bridge component name used in web calls
 * @property bridgeDelegate Delegate for handling bridge communication
 * 
 * @see BridgeComponent
 * @see android.widget.Toast
 * 
 * Usage from JavaScript:
 * ```javascript
 * window.BagistoNative.toast.show({
 *     message: 'Product added to cart!'
 * });
 * ```
 * 
 * @constructor
 * @param name Component identifier for the bridge
 * @param bridgeDelegate Bridge delegate for message handling
 */
class ToastComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val TAG = "ToastComponent"
    
    /**
     * Handle incoming messages from the web layer.
     * 
     * Processes the "show" event to display a toast message.
     * 
     * @param message The incoming message from web layer
     * 
     * @see Message
     */
    override fun onReceive(message: Message) {

        when (message.event) {
            "show" -> showToast(message)
            else -> Log.w("ToastComponent", "Unknown event for message: $message")
        }
    }

    /**
     * Display a native Android toast message.
     * 
     * Parses the message data and shows a short-duration toast.
     * 
     * @param message The message containing toast configuration
     * 
     * @see Toast
     * @see MessageData
     */
    private fun showToast(message: Message) {
        val data = message.data<MessageData>() ?: return

        val context = bridgeDelegate.destination.fragment.requireContext()
        Toast.makeText(context, data.message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Data class for toast message configuration.
     * 
     * @property message The text message to display in the toast
     */
    @Serializable
    data class MessageData(
        val message: String
    )
}
