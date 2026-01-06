package com.masilotti.demo.components

import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.compose.ui.platform.ComposeView
import com.masilotti.demo.helper.ToolbarButton
import com.masilotti.demo.utils.ApplicationConstants
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.fragments.HotwireFragment
import org.json.JSONObject

class LogoutComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val TAG = "LogoutComponent"
    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

    override fun onReceive(message: Message) {
        Log.d(TAG, "LogoutComponent message -> ${message}")
//        when(message.event){
//            "connect" -> addLogoutButton(message)
//        }

    }

    private fun addLogoutButton(message: Message) {
        Log.d(TAG, "LogoutComponent message 1 -> ${message}")
        Log.d(TAG, "LogoutComponent message 2 -> ${message.data<DynamicButtonComponent.MessageData>()}")
        val data = message.data<DynamicButtonComponent.MessageData>() ?: return
        Log.d(TAG, "LogoutComponent data -> ${data}")
        val logOutLink = JSONObject(message.jsonData).getString("logoutlink")
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

    private fun removeButton() {
        val toolbar = fragment.toolbarForNavigation()
        val button = toolbar?.findViewById<ComposeView>(ApplicationConstants.LOGOUT_BUTTON_ID)
        toolbar?.removeView(button)
    }

}