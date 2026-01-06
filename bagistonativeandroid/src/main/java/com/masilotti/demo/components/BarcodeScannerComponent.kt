package com.masilotti.demo.components

//class BarcodeScannerComponent {
//}

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

class BarcodeScannerComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val TAG = "BarcodeScannerComponent"
    private val buttonId = 1
    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

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

    private fun addButton(message: Message) {
        Log.d(TAG,"add button message 1 -> ${message}")
        Log.d(TAG,"add button message 2 -> ${message.data<MessageData>()}")
        val data = message.data<MessageData>() ?: return
        Log.d(TAG,"add button data -> ${data}")
        removeButton()
        val composeView = ComposeView(fragment.requireContext()).apply {
            id = buttonId
            setContent {
//                ToolbarButton(
//                    title = data.title,
//                    imageName = data.imageName,
//                    onClick = { replyTo(message.event) })
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

//@Composable
//private fun ToolbarButton(imageName: String, onClick: () -> Unit) {
//    Button(
//        onClick = onClick,
//        colors = ButtonDefaults.buttonColors(
//            containerColor = Color.Transparent,
//            contentColor = Color.Black
//        )
//    ) {
//        Text(
//            text = imageName,
//            fontFamily = FontFamily(Font(R.font.material_symbols)),
//            fontSize = 28.sp,
//            style = TextStyle(fontFeatureSettings = "liga")
//        )
//    }
//}



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

