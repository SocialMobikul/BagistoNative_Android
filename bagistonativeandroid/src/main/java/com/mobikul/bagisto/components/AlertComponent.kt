package com.mobikul.bagisto.components

import android.util.Log
import androidx.appcompat.app.AlertDialog
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.fragments.HotwireFragment
import kotlinx.serialization.Serializable

/**
 * Bridge component for displaying native Android alert dialogs.
 * 
 * This component enables the web layer to trigger native alert dialogs
 * with custom titles, messages, and action buttons. It provides a better
 * user experience than browser alerts.
 * 
 * @property name The bridge component name used in web calls
 * @property bridgeDelegate Delegate for handling bridge communication
 * 
 * @see BridgeComponent
 * @see HotwireDestination
 * @see HotwireFragment
 * 
 * Usage from JavaScript:
 * ```javascript
 * // Show confirmation dialog
 * window.BagistoNative.alert.show({
 *     title: 'Confirm Delete',
 *     description: 'Are you sure you want to delete this item?',
 *     destructive: true,
 *     confirm: 'Delete',
 *     dismiss: 'Cancel'
 * }).then(result => {
 *     console.log('User confirmed:', result);
 * });
 * ```
 * 
 * @constructor
 * @param name Component identifier for the bridge
 * @param bridgeDelegate Bridge delegate for message handling
 */
class AlertComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

    /**
     * Handle incoming messages from the web layer.
     * 
     * Processes the "show" event to display an alert dialog.
     * 
     * @param message The incoming message from web layer
     * 
     * @see Message
     */
    override fun onReceive(message: Message) {
        when (message.event) {
            "show" -> showAlert(message)
            else -> Log.w("AlertComponent", "Unknown event for message: $message")
        }
    }

    /**
     * Display a native Android alert dialog.
     * 
     * Parses the message data to extract title, description, buttons,
     * and creates an AlertDialog. On confirm, replies to the web layer.
     * 
     * @param message The message containing alert configuration
     * 
     * @see AlertDialog
     * @see MessageData
     */
    private fun showAlert(message: Message) {
        val data = message.data<MessageData>() ?: return

        AlertDialog.Builder(fragment.requireContext()).setTitle(data.title)
            .setMessage(data.description).setCancelable(true)
            .setNegativeButton(data.dismiss, null)
            .setPositiveButton(data.confirm) { _, _ ->
                replyTo(message.event)
            }.show()
    }

    /**
     * Data class for alert dialog configuration.
     * 
     * @property title The dialog title text
     * @property description The dialog message/description
     * @property destructive Whether the confirm button is destructive (red)
     * @property confirm Text for the confirm/positive button
     * @property dismiss Text for the dismiss/negative button
     */
    @Serializable
    private data class MessageData(
        val title: String,
        val description: String?,
        val destructive: Boolean,
        val confirm: String,
        val dismiss: String
    )
}
