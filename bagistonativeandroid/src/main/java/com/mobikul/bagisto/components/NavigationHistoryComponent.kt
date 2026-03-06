package com.mobikul.bagisto.components

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.R
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.mobikul.bagisto.utils.ApplicationConstants
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.fragments.HotwireFragment
import org.json.JSONObject

class NavigationHistoryComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val TAG = "historysync"
    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment


    override fun onReceive(message: Message) {
        Log.d(TAG, "historysync message -> $message")
        when (message.event) {
            "history" -> {
                handleShowBackButtonEvent(message)
            }
        }
    }




    private fun handleShowBackButtonEvent(message: Message) {
        try {
            val jsonData = JSONObject(message.jsonData)
            val metadata = jsonData.optJSONObject("metadata")
            val url = metadata?.optString("url")
            Log.d(TAG, "Root URL: ${ApplicationConstants.ROOT_URL}")
            val title = jsonData.optString("title", "")
            Log.d("check_pagetitle", "Page title: $title")
            val toolbar = fragment.toolbarForNavigation()
            toolbar?.title = title
            Log.d(TAG, "Extracted URL: $url")
            if (url != null && url != ApplicationConstants.ROOT_URL) {
                fragment.requireActivity().runOnUiThread {
                    try {
                        Log.d("Toolbar", "$toolbar")
                        if (toolbar != null && url!= ApplicationConstants.ROOT_URL) {
                            Log.d(TAG, "Toolbar found, showing back button")
                            showBackButtonUsingToolbar(toolbar)
                            Log.d(TAG, "Back button shown on toolbar")
                        } else {
                            Log.w(TAG, "No toolbar found via toolbarForNavigation()")
                            handleBackPress()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error showing back button", e)
                    }
                }
            } else {
                Log.w(TAG, "Invalid URL or URL is equal to ROOT_URL")
                handleBackPress()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing message JSON", e)
        }
    }


    private fun showBackButtonUsingToolbar(toolbar: Toolbar) {
        try {
            val backIcon = R.drawable.abc_ic_ab_back_material
            toolbar.navigationIcon = toolbar.context.getDrawable(backIcon)
            toolbar.setNavigationOnClickListener {
                Log.d(TAG, "Back button clicked, navigating back")
                if (canGoBackInWebView()) {
                    Log.d(TAG, "WebView can go back, navigating inside WebView")
                    bridgeDelegate.destination.navigator?.session?.webView?.goBack()
                } else {
                    Log.d(TAG, "WebView cannot go back, navigating back in fragment/activity")
                    fragment.requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
            Log.d(TAG, "Set default back icon and click listener-->")
            toolbar.navigationContentDescription = "Navigate up"
            Log.d(TAG, "Set default back icon and click listener")
        } catch (e: Exception) {
            Log.w(TAG, "Could not set default back icon, trying alternative", e)
            try {
                toolbar.navigationIcon = null
                fragment.requireActivity().let { activity ->
                    if (activity is AppCompatActivity) {
                        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        activity.supportActionBar?.setHomeButtonEnabled(true)
                    }
                }
                toolbar.setNavigationOnClickListener {
                    Log.d(TAG, "Back button clicked (alternative), navigating back")
                    if (canGoBackInWebView()) {
                        Log.d(TAG, "WebView can go back, navigating inside WebView")
                        bridgeDelegate.destination.navigator?.session?.webView?.goBack()
                    } else {
                        Log.d(TAG, "WebView cannot go back, navigating back in fragment/activity")
                        fragment.requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            } catch (e2: Exception) {
                Log.e(TAG, "Alternative approach also failed", e2)
            }
        }
        makeNavigationButtonVisible(toolbar)
    }

    private fun canGoBackInWebView(): Boolean {
        val canGoBack = bridgeDelegate.destination.navigator?.session?.webView?.canGoBack() ?: false
        Log.d(TAG, "WebView canGoBack: $canGoBack")
        return canGoBack
    }



    private fun makeNavigationButtonVisible(toolbar: Toolbar) {
        for (i in 0 until toolbar.childCount) {
            val child = toolbar.getChildAt(i)
            if (child is ImageView) {
                child.visibility = View.VISIBLE
                Log.d(TAG, "Made ImageView visible at index $i")
            }
            if (child is ViewGroup) {
                makeViewGroupChildrenVisible(child)
            }
        }
    }

    private fun makeViewGroupChildrenVisible(viewGroup: ViewGroup) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is ImageView) {
                child.visibility = View.VISIBLE
            }
            if (child is ViewGroup) {
                makeViewGroupChildrenVisible(child)
            }
        }
    }


    private fun handleBackPress() {
        fragment.requireActivity().runOnUiThread {
            try {
                val toolbar = fragment.toolbarForNavigation()
                if (toolbar != null) {
                    Log.d(TAG, "Toolbar found, attempting to hide back arrow")
                    toolbar.navigationIcon = null
                    toolbar.setNavigationOnClickListener(null)
                    hideToolbarNavigationIcon(toolbar)
                    Log.d(TAG, "Back arrow should be hidden from Toolbar")
                } else {
                    Log.w(TAG, "No toolbar found via fragment.toolbarForNavigation()")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error removing back button icon", e)
            }
        }
    }

    private fun hidebackbutton(message: Message) {
        fragment.requireActivity().runOnUiThread {
            try {
                val toolbar = fragment.toolbarForNavigation()
                if (toolbar != null) {
                    toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material)
                    toolbar.setNavigationOnClickListener {
                        fragment.requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                    Log.d(TAG, "Back arrow restored to Toolbar")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error showing back button", e)
            }
        }
    }

    private fun hideToolbarNavigationIcon(toolbar: Toolbar) {
        try {
            for (i in 0 until toolbar.childCount) {
                val child = toolbar.getChildAt(i)
                if (child is ImageView && child.drawable != null) {
                    child.visibility = View.GONE
                    Log.d(TAG, "Found and hid potential navigation ImageView")
                }
                if (child is ViewGroup) {
                    hideNavigationIconInViewGroup(child)
                }
            }
            val navIconId = toolbar.resources.getIdentifier("toolbar_navigation_button", "id", "android")
            if (navIconId != 0) {
                toolbar.findViewById<ImageView>(navIconId)?.visibility = View.GONE
                Log.d(TAG, "Hidden navigation icon by ID: $navIconId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in hideToolbarNavigationIcon", e)
        }
    }

    private fun hideNavigationIconInViewGroup(viewGroup: ViewGroup) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is ImageView && child.drawable != null) {
                child.visibility = View.GONE
                Log.d(TAG, "Found and hid ImageView in nested ViewGroup")
            }
            if (child is ViewGroup) {
                hideNavigationIconInViewGroup(child)
            }
        }
    }

}