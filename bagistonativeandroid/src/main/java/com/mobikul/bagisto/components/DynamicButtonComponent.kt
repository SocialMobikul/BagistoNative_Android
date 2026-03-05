package com.mobikul.bagisto.components

//class DynamicButtonComponent {
//}

import android.content.Intent
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.background
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
import com.mobikul.bagisto.R // Replace with your package name.
import com.mobikul.bagisto.components.features.QrScannerScreen
import com.mobikul.bagisto.components.features.image_search.ImageSearchScreen
import com.mobikul.bagisto.helper.ToastHelper
import com.mobikul.bagisto.utils.PermissionUtils
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.fragments.HotwireFragment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.mobikul.bagisto.view_model.SharedViewModel
import com.mobikul.bagisto.view_model.ViewModelProvider
import org.json.JSONObject
import androidx.compose.material.icons.filled.*
import com.mobikul.bagisto.helper.ToolbarButton
import com.mobikul.bagisto.utils.ThemeStateHolder

import androidx.activity.OnBackPressedCallback
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.view.KeyEvent

class DynamicButtonComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val buttonId = 1
    private val searchId = 9918
    private val TAG = "DynamicButtonComponent"
    private var isModalOpen = false
    private var lastMessage: Message? = null
    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

    private var activeOverlay: FrameLayout? = null

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            Log.d(TAG, "DynamicButtonComponent handleOnBackPressed fallback")
            // Priority 1: Dismiss scanner/image search overlay if open
            activeOverlay?.let { overlay ->
                Log.d(TAG, "Dismissing active overlay via back press")
                overlay.visibility = View.GONE
                overlay.removeAllViews()
                activeOverlay = null
                isEnabled = false
                return
            }
            // Priority 2: Collapse search view if expanded
            val toolbar = fragment.toolbarForNavigation()
            val searchView = toolbar?.findViewById<SearchView>(searchId)
            if (searchView != null && !searchView.isIconified) {
                collapseSearchView(searchView)
            } else {
                isEnabled = false
                fragment.requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onReceive(message: Message) {
        ViewModelProvider.initialize(fragment)

        Log.d(TAG,"bridgeDelegate.activeComponents -> ${bridgeDelegate.activeComponents}")
        Log.d(TAG,"bridgeDelegate -> ${bridgeDelegate}")
        Log.d(TAG,"bridgeDelegate -> ${bridgeDelegate.location}")
        Log.d("check_event", "dynamic message -> ${message}")
        when (message.event) {
            "connect","home"  -> {
                lastMessage = message
//                addSearchView(message)
                addHomeButton(message)
            }
            "product" -> {
                lastMessage = message
                addProductButton(message)
            }
            "account" -> {
                lastMessage = message
                addAccountButton(message)
            }
            "disconnect" -> {
                lastMessage = null
                removeButton()
            }
            "cartcount"->updateCartCount(message)
            "modalopen" -> {
                handleModalOpen(message)
            }
            "modaldismiss" -> {
                handleModelDismiss(message)
            }
//            "product" -> addProductButton()
            "empty" -> {
                lastMessage = null
                removeButton()
                // Only hide search explicitly when the dynamic area is cleared
                fragment.toolbarForNavigation()?.findViewById<View>(searchId)?.let {
                    fragment.toolbarForNavigation()?.removeView(it)
                }
            }

            else -> Log.w("DynamicButtonComponent", "Unknown event for message: $message")
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "DynamicButtonComponent onStart")
        fragment.requireActivity().onBackPressedDispatcher.addCallback(fragment.viewLifecycleOwner, backPressedCallback)
        lastMessage?.let { message ->
            when (message.event) {
                "connect", "home" -> addHomeButton(message)
                "product" -> addProductButton(message)
                "account" -> addAccountButton(message)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "DynamicButtonComponent onStop")
        hideKeyboard()
        val toolbar = fragment.toolbarForNavigation()
        toolbar?.findViewById<SearchView>(searchId)?.let { collapseSearchView(it) }
    }

    private fun hideKeyboard() {
        val view = fragment.view ?: return
        val imm = fragment.requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun collapseSearchView(searchView: SearchView) {
        Log.d(TAG, "DynamicButtonComponent collapseSearchView called")
        searchView.setQuery("", false)
        searchView.isIconified = true
        searchView.setIconified(true)
        searchView.onActionViewCollapsed()
        searchView.clearFocus()
        backPressedCallback.isEnabled = false
        
        searchView.layoutParams = Toolbar.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ).apply { gravity = Gravity.END }
        
        val toolbar = fragment.toolbarForNavigation()
        // Restore visibility of this component's button
        toolbar?.findViewById<View>(buttonId)?.visibility = View.VISIBLE

        hideKeyboard()
    }

    private fun handleModalOpen(message: Message) {
        val toolbar = fragment.toolbarForNavigation() ?: return

        isModalOpen = true

        toolbar.setNavigationIcon(
            com.google.android.material.R.drawable.ic_m3_chip_close
        )

        toolbar.setNavigationOnClickListener {
            // CLOSE icon behavior
            replyTo(message.event, jsonData = """{"type":"modal_dismiss"}""")
        }
    }



    private fun handleModelDismiss(message: Message) {
        replyTo(message.event, jsonData = """{"type": "modal_dismiss"}""")
        val toolbar = fragment.toolbarForNavigation() ?: return
        val webView = bridgeDelegate.destination.navigator.session.webView

        isModalOpen = false

        if (webView.canGoBack()) {
            toolbar.setNavigationIcon(
                com.google.android.material.R.drawable.ic_arrow_back_black_24
            )

            toolbar.setNavigationOnClickListener {
                webView.goBack()
            }
        } else {
            toolbar.navigationIcon = null
            toolbar.setNavigationOnClickListener(null)
        }
    }



    private fun addAccountButton(message: Message) {
        Log.d("check_event", "add button message 1 -> ${message}")
        Log.d("check_event", "add button message 2 -> ${message.data<MessageData>()}")
        val data = message.data<MessageData>() ?: return
        Log.d("check_event", "add button data -> ${data}")
        removeButton()
        val composeView = ComposeView(fragment.requireContext()).apply {
            id = buttonId
            setContent {
                    ToolbarButton(
                        imageName = if (ThemeStateHolder.isDarkTheme.value) "dark_mode" else "sunny",
                        onClick = {
                            if (ThemeStateHolder.isDarkTheme.value){
                                // This is tricky as we are toggling. 
                                // But since we are calling setDefaultNightMode, 
                                // the user's click should correspond to what they SEE.
                                replyTo(message.event, jsonData = """{"type": "theme","code": "light"}""")
                            }
                            else{
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
                                    val activity = fragment.requireActivity()
                                    val overlay = getOrCreateScannerOverlay(activity)

                                    overlay.removeAllViews()
                                    overlay.visibility = View.VISIBLE
                                    activeOverlay = overlay
                                    backPressedCallback.isEnabled = true

                                    val composeView = ComposeView(activity).apply {
                                        setContent {
                                            ImageSearchScreen(
                                                onLabelSelected = { label ->
                                                    overlay.visibility = View.GONE
                                                    overlay.removeAllViews()
                                                    activeOverlay = null
                                                    backPressedCallback.isEnabled = false
                                                    replyTo(message.event, """{"type": "scan", "code": "$label"}""")
                                                },
                                                onBack = {
                                                    overlay.visibility = View.GONE
                                                    overlay.removeAllViews()
                                                    activeOverlay = null
                                                    backPressedCallback.isEnabled = false
                                                }
                                            )


//                                            var showSelection by remember { mutableStateOf(true) }
//                                            var searchType by remember { mutableStateOf<SearchType?>(null) }
//
//                                            if (showSelection) {
//                                                ScanTypeSelectionDialog(
//                                                    onImageSelected = {
//                                                        searchType = SearchType.IMAGE
//                                                        showSelection = false
//                                                    },
//                                                    onTextSelected = {
//                                                        searchType = SearchType.TEXT
//                                                        showSelection = false
//                                                    },
//                                                    onDismiss = {
//                                                        overlay.visibility = View.GONE
//                                                        overlay.removeAllViews()
//                                                    }
//                                                )
//                                            } else when (searchType) {
//                                                SearchType.IMAGE -> ImageSearchScreen(
//                                                    onLabelSelected = { label ->
//                                                        overlay.visibility = View.GONE
//                                                        overlay.removeAllViews()
//                                                        replyTo(message.event, """{"type": "scan", "code": "$label"}""")
//                                                    },
//                                                    onBack = {
//                                                        overlay.visibility = View.GONE
//                                                        overlay.removeAllViews()
//                                                    }
//                                                )
//                                                SearchType.TEXT -> TextSearchScreen(
//                                                    onTextDetected = { text ->
//                                                        Log.d(TAG, "onTextDetected -> ${text}")
//                                                        overlay.visibility = View.GONE
//                                                        overlay.removeAllViews()
//                                                        replyTo(message.event, """{"type": "scan", "code": "$text"}""")
//                                                    },
//                                                    onBack = {
//                                                        overlay.visibility = View.GONE
//                                                        overlay.removeAllViews()
//                                                    }
//                                                )
//                                                null -> Unit
//                                            }
                                        }
                                    }
                                    overlay.addView(composeView)
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
        // Only remove the dynamic button, don't touch searchId here to avoid killing SearchComponent
        toolbar?.findViewById<View>(buttonId)?.let { toolbar.removeView(it) }
    }

    @Serializable
    data class MessageData(
        val title: String = "test title",
        @SerialName("androidImage") val imageName: String?
    )

    private fun showFullScreenScanner(message: Message) {
        val activity = fragment.requireActivity()
        val overlay = getOrCreateScannerOverlay(activity)

        overlay.removeAllViews()
        overlay.visibility = View.VISIBLE
        activeOverlay = overlay
        backPressedCallback.isEnabled = true

        ComposeView(activity).apply {
            setContent {
                QrScannerScreen(
                    onScanComplete = { result ->
                        overlay.visibility = View.GONE
                        overlay.removeAllViews()
                        activeOverlay = null
                        backPressedCallback.isEnabled = false
                        Log.d(TAG, "QR or bar code scan result -> $result")
                        replyTo(message.event, """{"type": "scan", "code": "$result"}""")
                    },
                    onBack = {
                        Log.d(TAG, "onback clicked")
                        overlay.visibility = View.GONE
                        overlay.removeAllViews()
                        activeOverlay = null
                        backPressedCallback.isEnabled = false
                    }
                )
            }
        }.also { overlay.addView(it) }
    }

    private fun getOrCreateScannerOverlay(activity: android.app.Activity): FrameLayout {
        val root = activity.findViewById<ViewGroup>(android.R.id.content)
        return root.findViewWithTag<FrameLayout>("DYNAMIC_SCANNER_OVERLAY")
            ?: FrameLayout(activity).apply {
                tag = "DYNAMIC_SCANNER_OVERLAY"
                root.addView(this, FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ))
            }
    }
    private fun updateCartCount(message: Message) {
        Log.d("check_eventtt", "add button message 1 -> ${message}")
        Log.d("check_eventtt", "add button message 2 -> ${message.data<MessageData>()}")
        val data = message.data<MessageData>() ?: return
        Log.d("check_eventtt", "add button data -> ${data}")
        val cartCount = JSONObject(message.jsonData).optString("count", "0").toInt()
        Log.d("check_eventtt", "cartCountt-> ${cartCount}")
        ViewModelProvider.get(SharedViewModel::class.java).updateCartCount(cartCount)

    }
    private fun addProductButton(message: Message) {
        Log.d("check_event", "add product button message 1 -> ${message}")
        Log.d("check_event", "add product button message 2 -> ${JSONObject(message.jsonData).optString("cart", "0")}")
        val data = message.data<MessageData>() ?: return
        Log.d("check_event", "add button data -> ${data}")
        val cartCount = JSONObject(message.jsonData).optString("cart", "0").toInt()
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
                    tint = if (ThemeStateHolder.isDarkTheme.value) Color.White else Color.Black
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
        val toolbar = fragment.toolbarForNavigation() ?: return
        Log.d(TAG,"DynamicButton addSearchView (remove-then-add)")
        
        // Remove existing first
        toolbar.findViewById<View>(searchId)?.let { 
            Log.d(TAG, "Removing existing search view before adding new one")
            toolbar.removeView(it) 
        }

        val searchView = object : SearchView(fragment.requireContext()) {
            override fun dispatchKeyEventPreIme(event: KeyEvent?): Boolean {
                if (event?.keyCode == KeyEvent.KEYCODE_BACK) {
                    if (event.action == KeyEvent.ACTION_UP) {
                        if (!isIconified) {
                            Log.d(TAG, "DynamicButton Search BACK UP detected via PreIme")
                            collapseSearchView(this)
                            return true
                        }
                    }
                }
                return super.dispatchKeyEventPreIme(event)
            }
        }.apply {
            id = searchId
            queryHint = "Search"
            isFocusable = true
            isFocusableInTouchMode = true

            setOnSearchClickListener {
                Log.d(TAG, "DynamicButton Search expanded")
                backPressedCallback.isEnabled = true
                layoutParams = Toolbar.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                // Hide dynamic buttons
                toolbar.findViewById<View>(buttonId)?.visibility = View.GONE
            }

            setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (hasFocus) backPressedCallback.isEnabled = true
            }

            setOnCloseListener {
                Log.d(TAG, "DynamicButton Search close icon clicked")
                collapseSearchView(this)
                true
            }

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    replyTo(message.event, """{"type": "scan", "code": "$query"}""")
                    return true
                }
                override fun onQueryTextChange(newText: String?): Boolean { return true }
            })
        }
        val layoutParams = Toolbar.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ).apply { gravity = Gravity.END }

        toolbar.addView(searchView, layoutParams)
    }

    @Serializable
    private data class QueryMessageData(val query: String?)


}

enum class SearchType { IMAGE, TEXT }