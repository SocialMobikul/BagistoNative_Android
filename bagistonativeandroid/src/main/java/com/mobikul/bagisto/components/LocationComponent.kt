package com.mobikul.bagisto.components

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.compose.ui.platform.ComposeView
import com.mobikul.bagisto.helper.LocationHelper
import com.mobikul.bagisto.helper.ToolbarButton
import com.mobikul.bagisto.utils.PermissionUtils
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.fragments.HotwireFragment

/**
 * Bridge component for GPS location services.
 * 
 * This component provides location functionality including:
 * - Adding location button to navigation toolbar
 * - Requesting location permissions
 * - Retrieving current GPS coordinates
 * - Returning latitude/longitude to web layer
 * 
 * @property name The bridge component name used in web calls
 * @property bridgeDelegate Delegate for handling bridge communication
 * 
 * @see LocationHelper
 * @see PermissionUtils
 * @see BridgeComponent
 * 
 * Required Permissions:
 * - ACCESS_FINE_LOCATION
 * - ACCESS_COARSE_LOCATION
 * 
 * Usage from JavaScript:
 * ```javascript
 * // Add location button to toolbar
 * window.BagistoNative.location.addLocationButton();
 * 
 * // Get current location (after button click)
 * window.BagistoNative.location.getLocation().then(location => {
 *     console.log('Latitude:', location.latitude);
 *     console.log('Longitude:', location.longitude);
 * });
 * ```
 * 
 * @constructor
 * @param name Component identifier for the bridge
 * @param bridgeDelegate Bridge delegate for message handling
 */
class LocationComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val buttonId = 1
    private val TAG = "LocationComponent"
    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

    /**
     * Handle incoming messages from the web layer.
     * 
     * Processes events like "addLocationButton" to add location
     * button to toolbar and handle location requests.
     * 
     * @param message The incoming message from web layer containing event and data
     * 
     * @see Message
     */
    override fun onReceive(message: Message) {
        Log.d(TAG, "LocationComponent message -> ${message}")
        when(message?.event){
            "addLocationButton" -> {
                requestLocationPermission(message)
                addLocationIcon(message)
            }
        }
    }

    /**
     * Request location permission from the user.
     * 
     * Uses PermissionUtils to request fine and coarse location
     * permissions from the user.
     * 
     * @param message The original message triggering the permission request
     * 
     * @see PermissionUtils
     */
    private fun requestLocationPermission(message: Message) {
        PermissionUtils.checkAndRequestLocationPermission(fragment.requireActivity()) { granted ->
            Log.d(TAG, "Location permission is granted -> $granted")
            if (granted) {
                Log.d(TAG, "Location permission granted, message -> $message")
            } else {
            }
        }
    }

    /**
     * Add location icon button to the navigation toolbar.
     * 
     * Creates a ComposeView with a location button that when clicked
     * will request location permission and then get the current GPS coordinates.
     * 
     * @param message The original message triggering the icon addition
     * 
     * @see ToolbarButton
     * @see ComposeView
     */
    private fun addLocationIcon(message: Message) {
        removeButton()
        val composeView = ComposeView(fragment.requireContext()).apply {
            id = buttonId
            setContent {
                ToolbarButton(
                    imageName = "location",
                    onClick = {
                        PermissionUtils.checkAndRequestLocationPermission(fragment.requireActivity()) { granted ->
                            Log.d(TAG, "Location permission is granted -> $granted")
                            if (granted) {
                                Log.d(TAG, "Location permission granted, message -> $message")
                                getLocation(message,fragment.requireContext())
                            } else {
                                requestLocationPermission(message)
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

    /**
     * Remove the location button from the toolbar.
     * 
     * Cleans up by removing the previously added location button
     * from the navigation toolbar.
     */
    private fun removeButton() {
        val toolbar = fragment.toolbarForNavigation()
        val button = toolbar?.findViewById<ComposeView>(buttonId)
        toolbar?.removeView(button)
    }

    /**
     * Get the current GPS location of the device.
     * 
     * Uses LocationHelper to obtain the current latitude and longitude,
     * then sends the coordinates back to the web layer via replyTo.
     * 
     * @param message The original message triggering the location request
     * @param context Android context for LocationHelper initialization
     * 
     * @see LocationHelper
     * @see BridgeComponent.replyTo
     */
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
            }
        }
    }
}
