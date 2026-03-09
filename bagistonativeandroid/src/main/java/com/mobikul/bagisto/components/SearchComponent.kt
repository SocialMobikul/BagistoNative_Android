package com.mobikul.bagisto.components

import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.fragments.HotwireFragment
import kotlinx.serialization.Serializable

import androidx.activity.OnBackPressedCallback
import android.content.Context
import android.graphics.Rect
import android.view.inputmethod.InputMethodManager
import android.view.KeyEvent
import android.view.ViewTreeObserver

/**
 * Bridge component for native search functionality in the toolbar.
 * 
 * This component provides a full-featured search experience including:
 * - Native Android SearchView in toolbar
 * - Keyboard handling and focus management
 * - Back press handling for search dismissal
 * - Query submission to web layer
 * - Automatic keyboard show/hide detection
 * 
 * @property name The bridge component name used in web calls
 * @property bridgeDelegate Delegate for handling bridge communication
 * 
 * @see BridgeComponent
 * @see androidx.appcompat.widget.SearchView
 * 
 * Usage from JavaScript:
 * ```javascript
 * // Connect/activate search
 * window.BagistoNative.search.connect();
 * 
 * // Clear/deactivate search
 * window.BagistoNative.search.empty();
 * 
 * // Listen for search query
 * window.BagistoNative.search.connect((query) => {
 *     console.log('Search query:', query);
 *     // Perform search
 * });
 * ```
 * 
 * @constructor
 * @param name Component identifier for the bridge
 * @param bridgeDelegate Bridge delegate for message handling
 */
class SearchComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val TAG = "SearchComponentAnchit"
    private val searchId = 9918
    private var lastMessage: Message? = null
    private var keyboardLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            Log.d(TAG, "SearchComponent handleOnBackPressed fallback")
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
     * Handle incoming messages from the web layer.
     * 
     * Processes events: "connect" to add SearchView, "empty" to remove it.
     * 
     * @param message The incoming message from web layer
     * 
     * @see Message
     * @see SearchView
     */
    override fun onReceive(message: Message) {
        Log.d(TAG,"SearchComponent message -> ${message}")
        when (message.event) {
            "connect" -> {
                lastMessage = message
                addSearchView()
            }
            "empty" -> {
                lastMessage = null
                val toolbar = fragment.toolbarForNavigation()
                toolbar?.findViewById<View>(searchId)?.let { toolbar.removeView(it) }
            }
            else -> Log.w("SearchComponent", "Unknown event for message: $message")
        }
    }

    /**
     * Called when the component's fragment starts.
     * 
     * Registers the back press callback and re-adds SearchView if
     * a previous connection message exists.
     * 
     * @see OnBackPressedCallback
     */
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "SearchComponent onStart")
        fragment.requireActivity().onBackPressedDispatcher.addCallback(fragment.viewLifecycleOwner, backPressedCallback)
        if (lastMessage != null) {
            addSearchView()
        }
    }

    /**
     * Called when the component's fragment stops.
     * 
     * Hides keyboard and collapses SearchView when navigating away.
     */
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "SearchComponent onStop")
        hideKeyboard()
        val toolbar = fragment.toolbarForNavigation()
        toolbar?.findViewById<SearchView>(searchId)?.let { collapseSearchView(it) }
    }

    /**
     * Hide the soft keyboard from the screen.
     * 
     * Uses InputMethodManager to hide the soft keyboard.
     */
    private fun hideKeyboard() {
        val view = fragment.view ?: return
        val imm = fragment.requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * Collapse and reset the SearchView to its iconified state.
     * 
     * Clears the query, removes focus, hides keyboard, and restores
     * the toolbar button visibility.
     * 
     * @param searchView The SearchView to collapse
     */
    private fun collapseSearchView(searchView: SearchView) {
        Log.d(TAG, "collapseSearchView called")
        if (!searchView.isIconified) {
            searchView.isIconified = true
        }
        searchView.setQuery("", false)
        searchView.clearFocus()
        backPressedCallback.isEnabled = false
        
        searchView.layoutParams = Toolbar.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ).apply { gravity = Gravity.END }
        
        val toolbar = fragment.toolbarForNavigation()
        toolbar?.findViewById<View>(1)?.visibility = View.VISIBLE

        hideKeyboard()
    }

    /**
     * Add SearchView to the navigation toolbar.
     * 
     * Creates a native Android SearchView, configures its listeners,
     * and adds it to the toolbar. Sets up query text submission handling.
     * 
     * @see SearchView
     * @see androidx.appcompat.widget.SearchView
     */
    private fun addSearchView() {
        val toolbar = fragment.toolbarForNavigation() ?: return
        Log.d(TAG,"SearchComponent addSearchView (remove-then-add)")
        
        toolbar.findViewById<View>(searchId)?.let { 
            Log.d(TAG, "Removing existing search view before adding new one")
            toolbar.removeView(it) 
        }

        val searchView = object : SearchView(fragment.requireContext()) {
            override fun dispatchKeyEventPreIme(event: KeyEvent?): Boolean {
                if (event?.keyCode == KeyEvent.KEYCODE_BACK) {
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        if (!isIconified) {
                            Log.d(TAG, "dispatchKeyEventPreIme BACK DOWN detected")
                            post { collapseSearchView(this) }
                            return true
                        }
                    } else if (event.action == KeyEvent.ACTION_UP) {
                        if (!isIconified) return true
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
                Log.d(TAG, "SearchComponent expanded")
                backPressedCallback.isEnabled = true
                layoutParams = Toolbar.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                toolbar.findViewById<View>(1)?.visibility = View.GONE
            }

            setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (hasFocus) backPressedCallback.isEnabled = true
            }

            setOnCloseListener {
                Log.d(TAG, "SearchComponent close icon clicked")
                post { collapseSearchView(this) }
                false
            }

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    replyTo("connect", QueryMessageData(query))
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
        setupKeyboardListener(searchView, toolbar)
    }

    /**
     * Setup keyboard visibility listener.
     * 
     * Monitors keyboard show/hide state and automatically collapses
     * SearchView when keyboard is dismissed.
     * 
     * @param searchView The SearchView to monitor
     * @param toolbar The toolbar containing the SearchView
     * 
     * @see ViewTreeObserver
     */
    private fun setupKeyboardListener(searchView: SearchView, toolbar: Toolbar) {
        val rootView = fragment.requireActivity().window.decorView
        var wasKeyboardVisible = false

        keyboardLayoutListener?.let {
            try { rootView.viewTreeObserver.removeOnGlobalLayoutListener(it) } catch (e: Exception) {}
        }

        keyboardLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            val r = Rect()
            rootView.getWindowVisibleDisplayFrame(r)
            val screenHeight = rootView.rootView.height
            val keypadHeight = screenHeight - r.bottom
            val isKeyboardVisible = keypadHeight > screenHeight * 0.15

            if (wasKeyboardVisible && !isKeyboardVisible) {
                if (!searchView.isIconified) {
                    collapseSearchView(searchView)
                    backPressedCallback.isEnabled = false
                }
            }
            wasKeyboardVisible = isKeyboardVisible
        }
        rootView.viewTreeObserver.addOnGlobalLayoutListener(keyboardLayoutListener)
    }

    @Serializable
    private data class QueryMessageData(val query: String?)
}
