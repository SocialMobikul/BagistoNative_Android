package com.masilotti.demo.components

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.R
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.masilotti.demo.utils.ApplicationConstants
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
            // Parse the JSON data from the message
            val jsonData = JSONObject(message.jsonData)
            val metadata = jsonData.optJSONObject("metadata") // Using optJSONObject to avoid null pointer exception
            val url = metadata?.optString("url")
            Log.d(TAG, "Root URL: ${ApplicationConstants.ROOT_URL}")

            // Safe extraction of the URL
            Log.d(TAG, "Extracted URL: $url")
            // Check if the URL is not null and not equal to the ROOT_URL
            if (url != null && url != ApplicationConstants.ROOT_URL) {
                fragment.requireActivity().runOnUiThread {
                    try {
                        // Get the toolbar using fragment.toolbarForNavigation()
                        val toolbar = fragment.toolbarForNavigation()
                        Log.d("Toolbar", "$toolbar")
                        if (toolbar != null && url!= ApplicationConstants.ROOT_URL) {
                            Log.d(TAG, "Toolbar found, showing back button")

                            // Show the back button using Toolbar's built-in navigation methods
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
        // Set a navigation icon - the system will handle positioning it on the left
        // Using the default back icon from AppCompat
        try {
            // Get the default back icon resource
            val backIcon = R.drawable.abc_ic_ab_back_material

            // Set the navigation icon
            toolbar.navigationIcon = toolbar.context.getDrawable(backIcon)

            // Set the click listener for back navigation
            toolbar.setNavigationOnClickListener {
                Log.d(TAG, "Back button clicked, navigating back")
                // Check if WebView can go back before triggering the fragment's back press
                if (canGoBackInWebView()) {
                    Log.d(TAG, "WebView can go back, navigating inside WebView")
                    // If WebView can go back, trigger the WebView's back navigation
                    bridgeDelegate.destination.navigator?.session?.webView?.goBack()
                } else {
                    Log.d(TAG, "WebView cannot go back, navigating back in fragment/activity")
                    // If WebView cannot go back, trigger the normal fragment/activity back press
                    fragment.requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
            Log.d(TAG, "Set default back icon and click listener-->")
            // Set navigation content description (accessibility)
            toolbar.navigationContentDescription = "Navigate up"

            Log.d(TAG, "Set default back icon and click listener")
        } catch (e: Exception) {
            Log.w(TAG, "Could not set default back icon, trying alternative", e)

            // Alternative approach if default icon fails
            try {
                // Try to set a simple back indicator
                toolbar.navigationIcon = null // Clear first

                // Try with a different approach
                fragment.requireActivity().let { activity ->
                    if (activity is AppCompatActivity) {
                        // Enable display home as up for the action bar
                        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        activity.supportActionBar?.setHomeButtonEnabled(true)
                    }
                }

                // Set navigation click listener
                toolbar.setNavigationOnClickListener {
                    Log.d(TAG, "Back button clicked (alternative), navigating back")
                    // Check if WebView can go back before triggering the fragment's back press
                    if (canGoBackInWebView()) {
                        Log.d(TAG, "WebView can go back, navigating inside WebView")
                        // If WebView can go back, trigger the WebView's back navigation
                        bridgeDelegate.destination.navigator?.session?.webView?.goBack()
                    } else {
                        Log.d(TAG, "WebView cannot go back, navigating back in fragment/activity")
                        // If WebView cannot go back, trigger the normal fragment/activity back press
                        fragment.requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }

            } catch (e2: Exception) {
                Log.e(TAG, "Alternative approach also failed", e2)
            }
        }

        // Make sure the toolbar navigation button is visible
        makeNavigationButtonVisible(toolbar)
    }

    private fun canGoBackInWebView(): Boolean {
        // Check if the WebView is active and can go back
        val canGoBack = bridgeDelegate.destination.navigator?.session?.webView?.canGoBack() ?: false
        Log.d(TAG, "WebView canGoBack: $canGoBack")
        return canGoBack
    }



    private fun makeNavigationButtonVisible(toolbar: Toolbar) {
        // Try to find and make the navigation button visible
        for (i in 0 until toolbar.childCount) {
            val child = toolbar.getChildAt(i)

            // Look for ImageViews that might be the navigation button
            if (child is ImageView) {
                // Make all ImageViews visible (the navigation icon will be among them)
                child.visibility = View.VISIBLE
                Log.d(TAG, "Made ImageView visible at index $i")
            }

            // Recursively check in ViewGroups
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
                // Get the toolbar using the same method as DynamicButtonComponent
                val toolbar = fragment.toolbarForNavigation()

                if (toolbar != null) {
                    Log.d(TAG, "Toolbar found, attempting to hide back arrow")

                    // Method 1: Remove the navigation icon directly from Toolbar
                    toolbar.navigationIcon = null

                    // Method 2: Set null navigation click listener
                    toolbar.setNavigationOnClickListener(null)

                    // Method 3: Hide the actual navigation ImageView if it exists
                    hideToolbarNavigationIcon(toolbar)

                    // Send success response


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
                    // Restore default back arrow
                    toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material)

                    // Set default back behavior
                    toolbar.setNavigationOnClickListener {
                        fragment.requireActivity().onBackPressedDispatcher.onBackPressed()
                    }

                    // Show the navigation ImageView if hidden

                    Log.d(TAG, "Back arrow restored to Toolbar")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error showing back button", e)

            }
        }
    }

    private fun hideToolbarNavigationIcon(toolbar: Toolbar) {
        try {
            // Method 1: Search for navigation icon in toolbar's children
            for (i in 0 until toolbar.childCount) {
                val child = toolbar.getChildAt(i)
                if (child is ImageView && child.drawable != null) {
                    // This might be the navigation icon
                    child.visibility = View.GONE
                    Log.d(TAG, "Found and hid potential navigation ImageView")
                }

                // Recursively search in ViewGroups
                if (child is ViewGroup) {
                    hideNavigationIconInViewGroup(child)
                }
            }

            // Method 2: Try to find by common IDs
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