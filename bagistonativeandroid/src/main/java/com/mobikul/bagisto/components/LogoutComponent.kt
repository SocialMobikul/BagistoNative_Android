package com.mobikul.bagisto.components

import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.compose.ui.platform.ComposeView
import com.mobikul.bagisto.helper.ToolbarButton
import com.mobikul.bagisto.utils.ApplicationConstants
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.fragments.HotwireFragment
import org.json.JSONObject

/**
 * Bridge component for logout functionality.
 * 
 * This component adds a logout button to the toolbar that triggers
 * a logout event back to the web layer when clicked.
 * 
 * @property name The bridge component name used in web calls
 * @property bridgeDelegate Delegate for handling bridge communication
 * 
 * @see BridgeComponent
 * @see ApplicationConstants
 * 
 * Usage from JavaScript:
 * ```javascript
 * // Add logout button
 * window.BagistoNative.logout.connect({
 *     logoutlink: '/customer/logout'
 * });
 * ```
 * 
 * @constructor
 * @param name Component identifier for the bridge
 * @param bridgeDelegate Bridge delegate for message handling
 */
class LogoutComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val TAG = "LogoutComponent"
    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

    /**
     * Handle incoming bridge messages.
     * 
     * Processes messages from the web layer for logout button
     * management. Currently logs messages for debugging.
     * 
     * @param message The incoming bridge message containing event and data
     */
    override fun onReceive(message: Message) {
        Log.d(TAG, "LogoutComponent message -> ${message}")

    }

    /**
     * Add logout button to the toolbar.
     * 
     * Creates a ComposeView with logout button and adds it to the
     * toolbar. When clicked, sends a logout event back to web layer.
     * Removes any existing logout button first.
     * 
     * @param message The message containing logout configuration
     */
    private fun addLogoutButton(message: Message) {
        Log.d(TAG, "LogoutComponent message 1 -> ${message}")
        Log.d(TAG, "LogoutComponent message 2 -> ${message.data<DynamicButtonComponent.MessageData>()}")
        val data = message.data<DynamicButtonComponent.MessageData>() ?: return
        Log.d(TAG, "LogoutComponent data -> ${data}")
        val logOutLink = JSONObject(message.jsonData).optString("logoutlink", "")
        removeButton()
        val composeView = ComposeView(fragment.requireContext()).apply {
            id = ApplicationConstants.LOGOUT_BUTTON_ID
            setContent {
                ToolbarButton(
                    imageName = "logout",
                    onClick = {
                        Log.d(TAG, "logout clicked")
                        replyTo(
                            message.event,
                            jsonData = """{"type": "logout","code": "$logOutLink"}"""
                        )
                    }
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

    /**
     * Remove logout button from toolbar.
     * 
     * Finds and removes the logout button ComposeView from the
     * toolbar if it exists.
     */
    private fun removeButton() {
        val toolbar = fragment.toolbarForNavigation()
        val button = toolbar?.findViewById<ComposeView>(ApplicationConstants.LOGOUT_BUTTON_ID)
        toolbar?.removeView(button)
    }

}
