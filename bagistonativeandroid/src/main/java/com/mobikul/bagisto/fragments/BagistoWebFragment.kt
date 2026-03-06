package com.mobikul.bagisto.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import dev.hotwire.navigation.activities.HotwireActivity
import dev.hotwire.navigation.destinations.HotwireDestinationDeepLink
import dev.hotwire.navigation.fragments.HotwireWebFragment

@HotwireDestinationDeepLink(uri = "hotwire://fragment/web")
class BagistoWebFragment : HotwireWebFragment() {
    private val TAG = "BagistoWebFragment"

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val activity = requireActivity() as? HotwireActivity
            if (activity == null) {
                Log.w(TAG, "Activity is not HotwireActivity")
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
                isEnabled = true
                return
            }

            val navigator = activity.delegate.currentNavigator
            if (navigator == null) {
                Log.w(TAG, "Navigator is null")
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
                isEnabled = true
                return
            }

            val webView = navigator.session.webView

            when {
                webView.canGoBack() -> {
                    Log.d(TAG, "WebView has history - going back in WebView")
                    webView.goBack()
                }
                !navigator.isAtStartDestination() -> {
                    Log.d(TAG, "Fragment back stack exists - popping fragment")
                    navigator.pop()
                }
                else -> {
                    val toolbar = toolbarForNavigation()
                    if (toolbar != null && toolbar.navigationIcon != null) {
                        Log.d(TAG, "At start destination but toolbar has navigation icon - triggering it")
                        for (i in 0 until toolbar.childCount) {
                            val child = toolbar.getChildAt(i)
                            if (child is android.widget.ImageButton) {
                                child.performClick()
                                return
                            }
                        }
                    }

                    Log.d(TAG, "At start destination - closing app")
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backPressedCallback
        )
    }
}
