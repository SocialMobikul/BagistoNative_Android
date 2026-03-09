package com.mobikul.bagisto.components

import android.content.Intent
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.fragments.HotwireFragment
import kotlinx.serialization.Serializable

/**
 * Bridge component for native Android share functionality.
 * 
 * This component enables the web layer to share content via
 * Android's native share sheet using Intent.ACTION_SEND.
 * Adds a share button to the toolbar that opens the native
 * share dialog.
 * 
 * @property name The bridge component name used in web calls
 * @property bridgeDelegate Delegate for handling bridge communication
 * 
 * @see BridgeComponent
 * @see android.content.Intent
 * 
 * Usage from JavaScript:
 * ```javascript
 * // Add share button to toolbar
 * window.BagistoNative.share.connect({
 *     url: 'https://example.com/product'
 * });
 * 
 * // Remove share button
 * window.BagistoNative.share.disconnect();
 * ```
 * 
 * @constructor
 * @param name Component identifier for the bridge
 * @param bridgeDelegate Bridge delegate for message handling
 */
class ShareComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val buttonId = 1
    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

    /**
     * Handle incoming messages from the web layer.
     * 
     * Processes "connect" to add share button, "disconnect" to remove it.
     * 
     * @param message The incoming message from web layer
     * 
     * @see Message
     */
    override fun onReceive(message: Message) {
        when (message.event) {
            "connect" -> addButton(message)
            "disconnect" -> removeButton()
            else -> Log.w("ShareComponent", "Unknown event for message: $message")
        }
    }

    /**
     * Add share button to the navigation toolbar.
     * 
     * Creates a ComposeView with share icon button and adds
     * it to the toolbar.
     * 
     * @param message The message containing share URL
     * 
     * @see ToolbarButton
     */
    private fun addButton(message: Message) {
        removeButton()
        val data = message.data<MessageData>() ?: return

        val composeView = ComposeView(fragment.requireContext()).apply {
            id = buttonId
            setContent {
                ToolbarButton(
                    onClick = { share(data.url) })
            }
        }
        val layoutParams = Toolbar.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.END }

        val toolbar = fragment.toolbarForNavigation()
        toolbar?.addView(composeView, layoutParams)
    }

    /**
     * Remove share button from the toolbar.
     */
    private fun removeButton() {
        val toolbar = fragment.toolbarForNavigation()
        val button = toolbar?.findViewById<ComposeView>(buttonId)
        toolbar?.removeView(button)
    }

    /**
     * Launch native Android share sheet.
     * 
     * Creates an Intent.ACTION_SEND with the URL and shows
     * the system share chooser.
     * 
     * @param url The URL/text to share
     * 
     * @see Intent
     */
    private fun share(url: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        fragment.requireActivity().startActivity(Intent.createChooser(intent, "Share via"))
    }

    /**
     * Data class for share configuration.
     * 
     * @property url The URL to share
     */
    @Serializable
    data class MessageData(
        val url: String
    )
}

/**
 * Composable for share toolbar button.
 * 
 * Renders an IconButton with share icon.
 * 
 * @param onClick Callback when button is clicked
 * 
 * @see IconButton
 * @see Icon
 */
@Composable
private fun ToolbarButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Share",
            tint = Color.Black
        )
    }
}
