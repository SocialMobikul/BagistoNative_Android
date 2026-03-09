package com.mobikul.bagisto.components

import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.fragments.HotwireFragment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Bridge component for adding dynamic buttons to the navigation toolbar.
 * 
 * This component enables the web layer to add custom buttons
 * to the Android toolbar with support for icons and text.
 * 
 * @property name The bridge component name used in web calls
 * @property bridgeDelegate Delegate for handling bridge communication
 * 
 * @see BridgeComponent
 * @see androidx.compose.material3.Button
 * 
 * Usage from JavaScript:
 * ```javascript
 * // Add button to toolbar
 * window.BagistoNative.button.connect({
 *     title: 'Cart',
 *     imageName: 'cart_icon'
 * });
 * 
 * // Remove button
 * window.BagistoNative.button.disconnect();
 * ```
 * 
 * @constructor
 * @param name Component identifier for the bridge
 * @param bridgeDelegate Bridge delegate for message handling
 */
class ButtonComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val buttonId = 1
    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

    /**
     * Handle incoming messages from the web layer.
     * 
     * Processes "connect" to add button, "disconnect" to remove button.
     * 
     * @param message The incoming message from web layer
     * 
     * @see Message
     */
    override fun onReceive(message: Message) {
        when (message.event) {
            "connect" -> addButton(message)
            "disconnect" -> removeButton()
            else -> Log.w("ButtonComponent", "Unknown event for message: $message")
        }
    }

    /**
     * Add a button to the navigation toolbar.
     * 
     * Creates a ComposeView with ToolbarButton, configures layout,
     * and adds it to the toolbar.
     * 
     * @param message The message containing button configuration
     * 
     * @see ToolbarButton
     * @see ComposeView
     */
    private fun addButton(message: Message) {
        val data = message.data<MessageData>() ?: return
        removeButton()

        val composeView = ComposeView(fragment.requireContext()).apply {
            id = buttonId
            setContent {
                ToolbarButton(
                    title = data.title,
                    imageName = data.imageName,
                    onClick = { replyTo(message.event) })
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
     * Remove the button from the navigation toolbar.
     * 
     * Finds and removes the button view from the toolbar.
     */
    private fun removeButton() {
        val toolbar = fragment.toolbarForNavigation()
        val button = toolbar?.findViewById<ComposeView>(buttonId)
        toolbar?.removeView(button)
    }

    /**
     * Data class for button configuration.
     * 
     * @property title The button text label
     * @property imageName Optional image name for icon display
     */
    @Serializable
    data class MessageData(
        val title: String,
        @SerialName("androidImage") val imageName: String?
    )
}

/**
 * Composable for toolbar button display.
 * 
 * Renders a Material 3 Button with either an icon or text label.
 * 
 * @param title The button text label
 * @param imageName Optional image name for icon
 * @param onClick Callback when button is clicked
 * 
 * @see Button
 * @see Text
 */
@Composable
private fun ToolbarButton(title: String, imageName: String?, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Black
        )
    ) {
        imageName?.let {
            Text(
                text = it,
                fontSize = 28.sp,
                style = TextStyle(fontFeatureSettings = "liga")
            )
        } ?: Text(title)
    }
}
