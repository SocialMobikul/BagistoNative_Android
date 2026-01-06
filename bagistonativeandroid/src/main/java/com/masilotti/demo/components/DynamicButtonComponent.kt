package com.masilotti.demo.components

//class DynamicButtonComponent {
//}

import android.content.Intent
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masilotti.demo.R // Replace with your package name.
import com.masilotti.demo.components.features.QrScannerScreen
import com.masilotti.demo.components.features.image_search.ImageSearchScreen
import com.masilotti.demo.helper.ToastHelper
import com.masilotti.demo.utils.AppSharedPreference
import com.masilotti.demo.utils.PermissionUtils
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.fragments.HotwireFragment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.masilotti.demo.view_model.SharedViewModel
import com.masilotti.demo.view_model.ViewModelProvider
import org.json.JSONObject
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.masilotti.demo.components.features.image_search.TextSearchScreen
import com.masilotti.demo.helper.ToolbarButton
import com.masilotti.demo.utils.ScanTypeSelectionDialog

class DynamicButtonComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val buttonId = 1
    private val searchId = 9918
    private val TAG = "DynamicButtonComponent"
    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

    override fun onReceive(message: Message) {
        ViewModelProvider.initialize(fragment)

        Log.d(TAG,"bridgeDelegate.activeComponents -> ${bridgeDelegate.activeComponents}")
        Log.d(TAG,"bridgeDelegate -> ${bridgeDelegate}")
        Log.d(TAG,"bridgeDelegate -> ${bridgeDelegate.location}")
        Log.d("check_event", "dynamic message -> ${message}")
        when (message.event) {
            "connect","home"  -> {
//                addSearchView(message)
                addHomeButton(message)
            }
            "product" -> addProductButton(message)
            "account" -> addAccountButton(message)
            "disconnect" -> removeButton()
            "cartcount"->updateCartCount(message)
//            "product" -> addProductButton()
            else -> Log.w("DynamicButtonComponent", "Unknown event for message: $message")
        }
    }

    private fun addAccountButton(message: Message) {
        val isDark = AppSharedPreference.getDisplayTheme()
        Log.d("check_event", "add button message 1 -> ${message}")
        Log.d("check_event", "add button message 2 -> ${message.data<MessageData>()}")
        val data = message.data<MessageData>() ?: return
        Log.d("check_event", "add button data -> ${data}")
        removeButton()
        val composeView = ComposeView(fragment.requireContext()).apply {
            id = buttonId
            setContent {
                    ToolbarButton(
                        imageName = if (isDark) "dark_mode" else "sunny",
                        onClick = {
                            if (AppSharedPreference.getDisplayTheme()){
                                AppSharedPreference.setDisplayTheme(false)
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                                replyTo(message.event, jsonData = """{"type": "theme","code": "light"}""")
                            }
                            else{
                                AppSharedPreference.setDisplayTheme(true)
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                                replyTo(message.event, jsonData = """{"type": "theme","code": "dark"}""")
                            }
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

    @OptIn(ExperimentalGetImage::class)
    private fun addHomeButton(message: Message) {
        Log.d("check_event", "add button message 1 -> ${message}")
        Log.d("check_event", "add button message 2 -> ${message.data<MessageData>()}")
        val data = message.data<MessageData>() ?: return
        Log.d("check_event", "add button data -> ${data}")
//        val cartCount = JSONObject(message.jsonData).getString("cart").toInt()
//        ViewModelProvider.get(SharedViewModel::class.java).updateCartCount(cartCount)
        removeButton()
        val composeView = ComposeView(fragment.requireContext()).apply {
            id = buttonId
            setContent {
                Row {
//                    ToolbarButton(
//                        imageName = "image_search", // Make sure this icon exists in your font
//                        onClick = {
//                            // For Camera Permission
//                            PermissionUtils.checkAndRequestCameraPermission(fragment.requireActivity()) { granted ->
//                                if (granted) {
//                                    showImageSearchScreen(message)
//                                }
//                                else{
//                                    ToastHelper.showLongToast(fragment.requireContext(),fragment.requireContext().getString(R.string.camera_permission_not_granted))
//                                }
//                            }
//                            //showImageSearchScreen(message)
//                        }
//                    )
                    ToolbarButton(
                        imageName = "image_search",
                        onClick = {
                            PermissionUtils.checkAndRequestCameraPermission(fragment.requireActivity()) { granted ->
                                if (granted) {
                                    // Show selection dialog instead of directly opening image search
                                    val activity = fragment.requireActivity()
                                    val overlay = activity.findViewById<FrameLayout>(R.id.scanner_overlay)
                                    overlay.removeAllViews()
                                    overlay.visibility = View.VISIBLE

                                    ComposeView(activity).apply {
                                        setContent {
                                            var showSelection by remember { mutableStateOf(true) }
                                            var searchType by remember { mutableStateOf<SearchType?>(null) }

                                            if (showSelection) {
                                                ScanTypeSelectionDialog(
                                                    onImageSelected = {
                                                        searchType = SearchType.IMAGE
                                                        showSelection = false
                                                    },
                                                    onTextSelected = {
                                                        searchType = SearchType.TEXT
                                                        showSelection = false
                                                    },
                                                    onDismiss = {
                                                        overlay.removeAllViews()
                                                    }
                                                )
                                            } else when (searchType) {
                                                SearchType.IMAGE -> ImageSearchScreen(
                                                    onLabelSelected = { label ->
                                                        overlay.removeAllViews()
                                                        replyTo(message.event, """{"type": "scan", "code": "$label"}""")
                                                    },
                                                    onBack = {
                                                        overlay.removeAllViews()
                                                    }
                                                )
                                                SearchType.TEXT -> TextSearchScreen(
                                                    onTextDetected = { text ->
                                                        Log.d(TAG, "onTextDetected -> ${text}")
                                                        overlay.removeAllViews()
                                                        replyTo(message.event, """{"type": "scan", "code": "$text"}""")
                                                    },
                                                    onBack = {
                                                        overlay.removeAllViews()
                                                    }
                                                )
                                                null -> Unit
                                            }
                                        }
                                    }.also { overlay.addView(it) }
                                } else {
                                    ToastHelper.showLongToast(fragment.requireContext(),
                                        fragment.requireContext().getString(R.string.camera_permission_not_granted))
                                }
                            }
                        }
                    )



                    ToolbarButton(
                        imageName = "qr_code_scanner",
                        onClick = {
                            // For Camera Permission
                            PermissionUtils.checkAndRequestCameraPermission(fragment.requireActivity()) { granted ->
                                if (granted) {
                                    showFullScreenScanner(message)
                                }
                                else{
                                    ToastHelper.showLongToast(fragment.requireContext(),fragment.requireContext().getString(R.string.camera_permission_not_granted))
                                }
                            }
                        }
                    )
                    CartIconWithBadge(ViewModelProvider.get(SharedViewModel::class.java).cartCount.value, onClick = {
                        Log.d(TAG, "cart icon clicked ")
                        replyTo(message.event, jsonData = """{"type": "cart"}""")
                    })

                }
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

    private fun showFullScreenScanner(message: Message) {
        val activity = fragment.requireActivity()
        val overlay = activity.findViewById<FrameLayout>(R.id.scanner_overlay)

        // Clear any previous views
        overlay.removeAllViews()
        overlay.visibility = View.VISIBLE

        // Add the scanner to overlay
        ComposeView(activity).apply {
            setContent {
                QrScannerScreen(
                    onScanComplete = { result ->
//                        overlay.visibility = View.GONE
                        overlay.removeAllViews()
                        Log.d(TAG,"QR or bar code scan result -> ${result}")
                        replyTo(message.event, """{"type": "scan", "code": "$result"}""")
                    },
                    onBack = {
                        Log.d(TAG,"onback clicked")
                        //overlay.visibility = View.GONE
                        overlay.removeAllViews()
                    }
                )
            }
        }.also { overlay.addView(it) }
    }
    private fun updateCartCount(message: Message) {
        Log.d("check_eventtt", "add button message 1 -> ${message}")
        Log.d("check_eventtt", "add button message 2 -> ${message.data<MessageData>()}")
        val data = message.data<MessageData>() ?: return
        Log.d("check_eventtt", "add button data -> ${data}")
        val cartCount = JSONObject(message.jsonData).getString("count").toInt()
        Log.d("check_eventtt", "cartCountt-> ${cartCount}")
        ViewModelProvider.get(SharedViewModel::class.java).updateCartCount(cartCount)

    }
    private fun addProductButton(message: Message) {
        Log.d("check_event", "add product button message 1 -> ${message}")
        Log.d("check_event", "add product button message 2 -> ${JSONObject(message.jsonData).getString("cart")}")
        val data = message.data<MessageData>() ?: return
        Log.d("check_event", "add button data -> ${data}")
        val cartCount = JSONObject(message.jsonData).getString("cart").toInt()
        ViewModelProvider.get(SharedViewModel::class.java).updateCartCount(cartCount)
        removeButton()
        val composeView = ComposeView(fragment.requireContext()).apply {
            id = buttonId
            setContent {
                Row {

                    ToolbarButton(
                        imageName = "Share",
                        onClick = {
                            Log.d(TAG,"share button clicked")
                            message.metadata?.let {
                                share(message.metadata!!.url)
                            }

                        }
                    )

                    CartIconWithBadge(ViewModelProvider.get(SharedViewModel::class.java).cartCount.value, onClick = {
                        Log.d(TAG, "cart icon clicked ")
                        replyTo(message.event, jsonData = """{"type": "cart"}""")
                    })
                }
            }
        }

        val layoutParams = Toolbar.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.END }

        val toolbar = fragment.toolbarForNavigation()
        toolbar?.addView(composeView, layoutParams)
    }



    private fun showImageSearchScreen(message: Message) {
        val activity = fragment.requireActivity()
        val overlay = activity.findViewById<FrameLayout>(R.id.scanner_overlay)

        overlay.removeAllViews()
        overlay.visibility = View.VISIBLE

        ComposeView(activity).apply {
            setContent {
                ImageSearchScreen(
                    onLabelSelected = { label ->
//                        overlay.visibility = View.GONE
                        overlay.removeAllViews()
                        Log.d(TAG, "Image search result -> $label")
                        replyTo(message.event, """{"type": "scan", "code": "$label"}""")
                    },
                    onBack = {
                        //overlay.visibility = View.GONE
                        overlay.removeAllViews()
                        Log.d(TAG, "Back pressed in image search")
                    }
                )
            }
        }.also { overlay.addView(it) }
    }

    private fun share(url: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }

        val context = fragment.requireContext()
        val chooser = Intent.createChooser(shareIntent, "Share via")
        context.startActivity(chooser)
    }

    @Composable
    fun CartIconWithBadge(
        cartCount: Int,
        onClick: () -> Unit
    ) {
        Box(
            modifier = Modifier
                .size(48.dp) // Adjust based on your TopAppBar height
                .padding(end = 8.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Cart",
                    tint = if (isSystemInDarkTheme()) Color.White else Color.Black
                )
            }

            if (cartCount > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = (-4).dp, y = 4.dp)
                        .size(16.dp)
                        .background(Color.Red, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cartCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    private fun addSearchView(message: Message) {
        Log.d(TAG,"SearchComponent added")
        if (fragment.toolbarForNavigation()?.findViewById<SearchView>(searchId) != null) return

        val toolbar = fragment.toolbarForNavigation()
        val searchView = SearchView(fragment.requireContext()).apply {
            id = searchId

            queryHint = "Search"
            isFocusable = true
            isFocusableInTouchMode = true

            setOnSearchClickListener {
                layoutParams = Toolbar.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            setOnCloseListener {
                layoutParams = Toolbar.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ).apply { gravity = Gravity.END }
                false
            }

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    //replyTo("connect", QueryMessageData(query))
                    replyTo(message.event, """{"type": "scan", "code": "$query"}""")
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    //replyTo("connect", QueryMessageData(newText))
                    return true
                }
            })
        }
        val layoutParams = Toolbar.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ).apply { gravity = Gravity.END }

        toolbar?.addView(searchView, layoutParams)
    }

    @Serializable
    private data class QueryMessageData(val query: String?)


}

enum class SearchType { IMAGE, TEXT }