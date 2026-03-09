package com.mobikul.bagisto.components

import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.mobikul.bagisto.R
import com.mobikul.bagisto.utils.ThemeStateHolder
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.fragments.HotwireFragment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Bridge component for adding dropdown menu to the navigation toolbar.
 * 
 * This component enables the web layer to add a context menu
 * (three-dot menu) to the Android toolbar with customizable menu items.
 * 
 * @property name The bridge component name used in web calls
 * @property bridgeDelegate Delegate for handling bridge communication
 * 
 * @see BridgeComponent
 * @see androidx.compose.material3.DropdownMenu
 * 
 * Usage from JavaScript:
 * ```javascript
 * // Add menu with items
 * window.BagistoNative.menu.connect({
 *     items: [
 *         { title: 'Profile', imageName: 'user' },
 *         { title: 'Settings', imageName: 'settings' },
 *         { title: 'Logout', imageName: 'logout' }
 *     ]
 * });
 * 
 * // Listen for selection
 * window.BagistoNative.menu.connect((index) => {
 *     console.log('Selected item:', index);
 * });
 * ```
 * 
 * @constructor
 * @param name Component identifier for the bridge
 * @param bridgeDelegate Bridge delegate for message handling
 */
class MenuComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val menuViewId = 42

    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

    /**
     * Handle incoming messages from the web layer.
     * 
     * Processes "connect" to add menu, "disconnect" to remove it.
     * 
     * @param message The incoming message from web layer
     * 
     * @see Message
     */
    override fun onReceive(message: Message) {
        when (message.event) {
            "connect" -> addMenuButton(message)
            "disconnect" -> removeMenuButton()
            else -> Log.w("MenuComponent", "Unknown event: ${message.event}")
        }
    }

    private fun addMenuButton(message: Message) {
        val actionBar = fragment.requireActivity().actionBar
        actionBar?.setDisplayShowHomeEnabled(true)
        actionBar?.setHomeButtonEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_launcher_foreground)

        val data = message.data<MessageData>() ?: return

        val composeView = ComposeView(fragment.requireContext()).apply {
            id = menuViewId
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MenuDropdown(
                    data = data,
                    onItemSelected = { index ->
                        replyTo(message.event, SelectionMessageData(index))
                    }
                )
            }
        }

        val layoutParams = Toolbar.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.END
        }

        val toolbar = fragment.toolbarForNavigation()
        toolbar?.addView(composeView, layoutParams)
    }

    private fun removeMenuButton() {
        val toolbar = fragment.toolbarForNavigation()
        val existingView = toolbar?.findViewById<ComposeView>(menuViewId)
        toolbar?.removeView(existingView)
    }

    @Serializable
    data class MessageData(
        val items: List<Item>
    )

    @Serializable
    data class Item(
        val title: String,
        @SerialName("androidImage") val imageName: String?
    )

    @Serializable
    data class SelectionMessageData(val index: Int)
}

@Composable
private fun MenuDropdown(
    data: MenuComponent.MessageData, onItemSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = !expanded }) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Menu",
            tint = if (ThemeStateHolder.isDarkTheme.value) Color.White else Color.Black
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        data.items.forEachIndexed { index, item ->
            DropdownMenuItem(
                trailingIcon = {
                    item.imageName?.let {
                        Text(
                            text = it,
                            fontSize = 20.sp,
                            style = TextStyle(
                                fontFeatureSettings = "liga"
                            )
                        )
                    }
                },
                text = { Text(item.title) },
                onClick = {
                    expanded = false
                    onItemSelected(index)
                }
            )
        }
    }
}
