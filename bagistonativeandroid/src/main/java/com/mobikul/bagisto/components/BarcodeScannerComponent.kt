package com.mobikul.bagisto.components

import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.fragments.HotwireFragment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner

/**
 * Bridge component for barcode scanning functionality.
 * 
 * This component uses Google ML Kit Barcode Scanning to detect
 * and decode barcodes from the camera feed.
 * 
 * Supported barcode formats:
 * - QR Code
 * - EAN-13, EAN-8
 * - UPC-A, UPC-E
 * - Code 39, Code 93, Code 128
 * - ITF, Codabar
 * - PDF417, Data Matrix, Aztec
 * 
 * @property name The bridge component name used in web calls
 * @property bridgeDelegate Delegate for handling bridge communication
 * @property activity The host activity for camera operations
 * 
 * @see BridgeComponent
 * @see ApplicationConstants
 * 
 * @constructor
 * @param name Component identifier for the bridge
 * @param bridgeDelegate Bridge delegate for message handling
 * @param activity Host activity for camera access
 */
class BarcodeScannerComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val TAG = "BarcodeScannerComponent"
    private val buttonId = 1
    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

    /**
     * Handle incoming messages from the web layer.
     * 
     * Processes "connect", "home" (add scanner button) and
     * "disconnect" (remove button) events.
     * 
     * @param message The incoming message from web layer
     * 
     * @see Message
     */
    override fun onReceive(message: Message) {
        print("fgtgfhfghfg")
        Log.d(TAG,"dynamic message -> ${message}")
        when (message.event) {
            "connect" -> addButton(message)
            "home" -> addButton(message)
            "disconnect" -> removeButton()
            else -> Log.w(TAG, "Unknown event for message: $message")
        }
    }

    /**
     * Add scanner button to the toolbar.
     * 
     * Creates a QR code scanner icon button that when clicked
     * opens the camera for barcode scanning.
     * 
     * @param message The message containing button configuration
     * 
     * @see QrScannerScreen
     */
    private fun addButton(message: Message) {
        Log.d(TAG,"add button message 1 -> ${message}")
        Log.d(TAG,"add button message 2 -> ${message.data<MessageData>()}")
        val data = message.data<MessageData>() ?: return
        Log.d(TAG,"add button data -> ${data}")
        removeButton()
        val composeView = ComposeView(fragment.requireContext()).apply {
            id = buttonId
            setContent {
                ToolbarButton(
                    onClick = {
                        Log.d(TAG,"home icon clicked")
                        replyTo(message.event)
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
        val button = toolbar?.findViewById<ComposeView>(buttonId)
        toolbar?.removeView(button)
    }

    @Serializable
    data class MessageData(
        val title: String = "test title",
        @SerialName("androidImage") val imageName: String?
    )
}

@Composable
private fun ToolbarButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Filled.QrCodeScanner,
            contentDescription = "Barcode Scanner",
            tint = Color.Black
        )
    }
}
