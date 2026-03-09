package com.mobikul.bagisto.components

import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.fragments.HotwireFragment
import kotlinx.serialization.Serializable

/**
 * Bridge component for form submission handling.
 * 
 * This component provides a native submit button in the toolbar
 * that can be enabled/disabled based on form validation. It allows
 * the web layer to control button state and receive submission events.
 * 
 * @property name The bridge component name used in web calls
 * @property bridgeDelegate Delegate for handling bridge communication
 * 
 * @see BridgeComponent
 * @see androidx.compose.material3.Button
 * 
 * Usage from JavaScript:
 * ```javascript
 * // Add submit button
 * window.BagistoNative.form.connect({
 *     title: 'Submit Order'
 * });
 * 
 * // Enable/disable button
 * window.BagistoNative.form.enableSubmit();
 * window.BagistoNative.form.disableSubmit();
 * 
 * // Remove button
 * window.BagistoNative.form.disconnect();
 * ```
 * 
 * @constructor
 * @param name Component identifier for the bridge
 * @param bridgeDelegate Bridge delegate for message handling
 */
class FormComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val buttonId = 1
    private var isButtonEnabled: MutableState<Boolean>? = null
    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

    /**
     * Handle incoming messages from the web layer.
     * 
     * Processes events: "connect" (add button), "disconnect" (remove),
     * "enableSubmit" (enable), "disableSubmit" (disable).
     * 
     * @param message The incoming message from web layer
     * 
     * @see Message
     */
    override fun onReceive(message: Message) {
        when (message.event) {
            "connect" -> addButton(message)
            "disconnect" -> removeButton()
            "enableSubmit" -> enableButton()
            "disableSubmit" -> disableButton()
            else -> Log.w("FormComponent", "Unknown event for message: $message")
        }
    }

    /**
     * Add submit button to the toolbar.
     * 
     * Creates a Material 3 Button composable and adds it
     * to the navigation toolbar.
     * 
     * @param message The message containing button configuration
     * 
     * @see Button
     */
    private fun addButton(message: Message) {
        val data = message.data<MessageData>() ?: return
        removeButton()

        val composeView = ComposeView(fragment.requireContext()).apply {
            id = buttonId
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val enabledState = remember { mutableStateOf(true) }
                isButtonEnabled = enabledState

                SubmitButton(
                    title = data.title,
                    enabled = enabledState.value,
                    onClick = { replyTo(message.event) }
                )
            }
        }
        val layoutParams = Toolbar.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.END }

        val toolbar = fragment.toolbarForNavigation()
        toolbar?.addView(composeView, layoutParams)
    }

    private fun removeButton() {
        val toolbar = fragment.toolbarForNavigation()
        val button = toolbar?.findViewById<ComposeView>(buttonId)
        toolbar?.removeView(button)
    }

    private fun enableButton() {
        isButtonEnabled?.value = true
    }

    private fun disableButton() {
        isButtonEnabled?.value = false
    }

    @Serializable
    data class MessageData(
        val title: String
    )
}

@Composable
private fun SubmitButton(
    title: String, enabled: Boolean, onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent, contentColor = Color.Black
        )
    ) {
        Text(title)
    }
}
