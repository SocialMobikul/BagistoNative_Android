package com.mobikul.bagisto.components

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
import com.mobikul.bagisto.R
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

/**
 * Bridge component for dynamic button creation and management.
 * 
 * This component allows creating custom buttons that can trigger
 * arbitrary events back to the web layer.
 * 
 * Features:
 * - Custom button text and styling
 * - Custom action type definition
 * - Event triggering to web layer
 * 
 * @property name The bridge component name used in web calls
 * @property bridgeDelegate Delegate for handling bridge communication
 * 
 * @see BridgeComponent
 * @see ApplicationConstants
 * 
 * @constructor
 * @param name Component identifier for the bridge
 * @param bridgeDelegate Bridge delegate for message handling
 */
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
        /**
         * Handle back press when overlay or search is active.
         * 
         * Closes overlay or collapses search view instead of
         * navigating back in the web view.
         */
        override fun handleOnBackPressed() {
            Log.d(TAG, "DynamicButtonComponent handleOnBackPressed fallback")
            activeOverlay?.let { overlay ->
                Log.d(TAG, "Dismissing active overlay via back press")
                overlay.visibility = View.GONE
                overlay.removeAllViews()
                activeOverlay = null
                isEnabled = false
                return
            }
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

    /**
     * Handle incoming bridge messages for dynamic button actions.
     * 
     * Processes various events like connect, disconnect, cart updates,
     * modal open/close, and dispatches to appropriate handlers.
     * 
     * @param message The incoming bridge message containing event and data
     */
    override fun onReceive(message: Message) {
        ViewModelProvider.initialize(fragment)

        Log.d(TAG,"bridgeDelegate.activeComponents -> ${bridgeDelegate.activeComponents}")
        Log.d(TAG,"bridgeDelegate -> ${bridgeDelegate}")
        Log.d(TAG,"bridgeDelegate -> ${bridgeDelegate.location}")
        Log.d("check_event", "dynamic message -> ${message}")
        when (message.event) {
            "connect","home"  -> {
                lastMessage = message
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
            "empty" -> {
                lastMessage = null
                removeButton()
                fragment.toolbarForNavigation()?.findViewById<View>(searchId)?.let {
                    fragment.toolbarForNavigation()?.removeView(it)
                }
            }

            else -> Log.w("DynamicButtonComponent", "Unknown event for message: $message")
        }
    }

    /**
     * Called when the component becomes active.
     * 
     * Registers back press callback and restores last active button
     * if available (e.g., after fragment recreation).
     */
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

    /**
     * Called when the component stops.
     * 
     * Hides keyboard and collapses search view if open.
     */
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "DynamicButtonComponent onStop")
        hideKeyboard()
        val toolbar = fragment.toolbarForNavigation()
        toolbar?.findViewById<SearchView>(searchId)?.let { collapseSearchView(it) }
    }

    /**
     * Hide the soft keyboard.
     * 
     * Uses InputMethodManager to hide keyboard from the current view.
     */
    private fun hideKeyboard() {
        val view = fragment.view ?: return
        val imm = fragment.requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * Collapse and reset a SearchView.
     * 
     * Clears query, iconifies the search view, removes focus,
     * and restores button visibility. Also hides keyboard.
     * 
     * @param searchView The SearchView to collapse
     */
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
        toolbar?.findViewById<View>(buttonId)?.visibility = View.VISIBLE

        hideKeyboard()
    }

    /**
     * Handle modal open event.
     * 
     * Configures toolbar to show close button that sends modal_dismiss
     * event back to web layer.
     * 
     * @param message The message triggering modal open
     */
    private fun handleModalOpen(message: Message) {
        val toolbar = fragment.toolbarForNavigation() ?: return

        isModalOpen = true

        toolbar.setNavigationIcon(
            com.google.android.material.R.drawable.ic_m3_chip_close
        )

        toolbar.setNavigationOnClickListener {
            replyTo(message.event, jsonData = """{"type":"modal_dismiss"}""")
        }
    }



    /**
     * Handle modal dismiss event.
     * 
     * Sends dismiss event to web and restores navigation based on
     * whether web view can go back.
     * 
     * @param message The message triggering modal dismiss
     */
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



    /**
     * Add account/theme toggle button to toolbar.
     * 
     * Creates a button that toggles between light and dark theme
     * and sends appropriate event to web layer.
     * 
     * @param message The message containing button configuration
     */
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

    /**
     * Add home button with search and scanner options.
     * 
     * Creates toolbar with image search, QR scanner, and cart icon.
     * Image search opens overlay with ML Kit image labeling.
     * QR scanner opens full screen barcode scanner.
     * 
     * @param message The message containing button configuration
     */
    @OptIn(ExperimentalGetImage::class)
    private fun addHomeButton(message: Message) {
        Log.d("check_event", "add button message 1 -> ${message}")
        Log.d("check_event", "add button message 2 -> ${message.data<MessageData>()}")
        val data = message.data<MessageData>() ?: return
        Log.d("check_event", "add button data -> ${data}")
        removeButton()
        val composeView = ComposeView(fragment.requireContext()).apply {
            id = buttonId
            setContent {
                Row {
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

    /**
     * Remove dynamic button from toolbar.
     * 
     * Finds and removes the button ComposeView by its ID.
     */
    private fun removeButton() {
        val toolbar = fragment.toolbarForNavigation()
        toolbar?.findViewById<View>(buttonId)?.let { toolbar.removeView(it) }
    }

    /**
     * Data class for button message payload.
     * 
     * Represents the expected JSON structure when receiving button
     * configuration from the web layer.
     * 
     * @property title The title/label for the button
     * @property imageName Optional image resource name for button icon
     */
    @Serializable
    data class MessageData(
        val title: String = "test title",
        @SerialName("androidImage") val imageName: String?
    )

    /**
     * Show full-screen QR/barcode scanner.
     * 
     * Creates overlay with camera-based scanner that supports
     * QR codes and various barcode formats.
     * 
     * @param message The message for callback with scan result
     */
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

    /**
     * Get or create scanner overlay.
     * 
     * Creates a FrameLayout overlay tagged for scanner functionality,
     * or returns existing one if already created.
     * 
     * @param activity The activity to create overlay in
     * @return The scanner overlay FrameLayout
     */
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
    /**
     * Update cart badge count.
     * 
     * Parses cart count from message and updates ViewModel.
     * 
     * @param message The message containing cart count
     */
    private fun updateCartCount(message: Message) {
        Log.d("check_eventtt", "add button message 1 -> ${message}")
        Log.d("check_eventtt", "add button message 2 -> ${message.data<MessageData>()}")
        val data = message.data<MessageData>() ?: return
        Log.d("check_eventtt", "add button data -> ${data}")
        val cartCount = JSONObject(message.jsonData).optString("count", "0").toInt()
        Log.d("check_eventtt", "cartCountt-> ${cartCount}")
        ViewModelProvider.get(SharedViewModel::class.java).updateCartCount(cartCount)

    }
    /**
     * Add product page button with share and cart.
     * 
     * Creates toolbar button with share and cart icons for
     * product detail pages.
     * 
     * @param message The message containing button configuration
     */
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




    /**
     * Share URL using system share sheet.
     * 
     * Creates intent with URL and shows chooser dialog.
     * 
     * @param url The URL to share
     */
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
                .size(48.dp)
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
