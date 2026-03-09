package com.mobikul.bagisto.helper

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

/**
 * Helper class for obtaining device location using Google Play Services.
 * 
 * This class provides a simplified interface for GPS location services
 * using the FusedLocationProviderClient. It supports both one-time
 * location retrieval and continuous location updates.
 * 
 * @property context Android context for accessing location services
 * 
 * @see com.google.android.gms.location.FusedLocationProviderClient
 * @see androidx.lifecycle.LiveData
 * 
 * Usage:
 * ```kotlin
 * val locationHelper = LocationHelper(context)
 * locationHelper.getLastKnownLocation { location ->
 *     location?.let {
 *         println("Lat: ${it.latitude}, Lng: ${it.longitude}")
 *     }
 * }
 * ```
 * 
 * Note: Requires ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions.
 */
class LocationHelper(context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _locationLiveData = MutableLiveData<Location>()
    val locationLiveData: LiveData<Location> = _locationLiveData

    /**
     * Callback for receiving location updates.
     * 
     * Called when new location data is available.
     * 
     * @param locationResult Contains the latest location result
     */
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult.lastLocation?.let { location ->
                _locationLiveData.postValue(location)
            }
        }
    }

    /**
     * Get the last known location of the device.
     * 
     * Requests the most recent location from the FusedLocationProvider.
     * Requires location permissions.
     * 
     * @param callback Callback receiving the Location or null if unavailable
     * 
     * @SuppressLint("MissingPermission") - Permission check handled by caller
     */
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(callback: (Location?) -> Unit) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                callback(location)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                callback(null)
            }
    }

    /**
     * Start receiving continuous location updates.
     * 
     * Requests location updates at the specified interval.
     * Use stopLocationUpdates() to stop receiving updates.
     * 
     * @SuppressLint("MissingPermission") - Permission check handled by caller
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000
        ).build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    /**
     * Stop receiving location updates.
     * 
     * Removes location update requests from the FusedLocationProvider.
     */
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        /**
         * Check if location services are enabled on the device.
         * 
         * @param context Android context
         * @return true if GPS or Network provider is enabled
         */
        fun isLocationEnabled(context: Context): Boolean {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }
}
