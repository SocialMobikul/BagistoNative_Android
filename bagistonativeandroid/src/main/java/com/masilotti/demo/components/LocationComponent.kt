package com.masilotti.demo.components

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.compose.ui.platform.ComposeView
import com.masilotti.demo.helper.LocationHelper
import com.masilotti.demo.helper.ToolbarButton
import com.masilotti.demo.utils.PermissionUtils
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.fragments.HotwireFragment

class LocationComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val buttonId = 1
    private val TAG = "LocationComponent"
    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

    override fun onReceive(message: Message) {
        Log.d(TAG, "LocationComponent message -> ${message}")
        when(message?.event){
            //"addLocationButton" -> RequestLocationPermission(message)
            "addLocationButton" -> {
                requestLocationPermission(message)
                addLocationIcon(message)
            }
        }
//        val theme = JSONObject(message.jsonData).getString("theme")
//        Log.d(TAG, "theme is -> ${theme}")

    }

    private fun requestLocationPermission(message: Message) {
        PermissionUtils.checkAndRequestLocationPermission(fragment.requireContext()) { granted ->
            Log.d(TAG, "Location permission is granted -> $granted")
            if (granted) {
                Log.d(TAG, "Location permission granted, message -> $message")
                //addLocationIcon(message)
            } else {

//                ToastHelper.showLongToast(
//                    fragment.requireContext(),
//                    fragment.requireContext().getString(R.string.location_permission_not_granted)
//                )
            }
        }
    }

    private fun addLocationIcon(message: Message) {
        removeButton()
        val composeView = ComposeView(fragment.requireContext()).apply {
            id = buttonId
            setContent {
                ToolbarButton(
                    imageName = "location",
                    onClick = {
                        PermissionUtils.checkAndRequestLocationPermission(fragment.requireContext()) { granted ->
                            Log.d(TAG, "Location permission is granted -> $granted")
                            if (granted) {
                                Log.d(TAG, "Location permission granted, message -> $message")
                                //addLocationIcon(message)
                                getLocation(message,fragment.requireContext())
                            } else {
                                requestLocationPermission(message)
//                                ToastHelper.showLongToast(
//                                    fragment.requireContext(),
//                                    fragment.requireContext().getString(R.string.location_permission_not_granted)
//                                )
                            }
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

    private fun removeButton() {
        val toolbar = fragment.toolbarForNavigation()
        val button = toolbar?.findViewById<ComposeView>(buttonId)
        toolbar?.removeView(button)
    }

    private fun getLocation(message: Message, context: Context) {
        Log.d(TAG,"location icon pressed")
        val locationHelper = LocationHelper(context)
        locationHelper.getLastKnownLocation { location ->
            location?.let {
                val latitude = it.latitude
                val longitude = it.longitude
                Log.d(TAG,"latitue -> ${latitude}\n longitude -> ${longitude}\n event -> ${message.event}")
                replyTo(message.event, jsonData = """{"latitude": "$latitude","longitude": "$longitude"}""")
            } ?: run {
                Log.d(TAG,"location not available")
                // Location not available
            }
        }
    }
}