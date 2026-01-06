package com.masilotti.demo.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionUtils {

    // Store launchers and callbacks by activity hash code
    private val cameraLaunchers = mutableMapOf<Int, ActivityResultLauncher<String>>()
    private val notificationLaunchers = mutableMapOf<Int, ActivityResultLauncher<String>>()
    private val locationLaunchers = mutableMapOf<Int, ActivityResultLauncher<String>>()

    private val locationCallbacks = mutableMapOf<Int, (Boolean) -> Unit>()
    private val cameraCallbacks = mutableMapOf<Int, (Boolean) -> Unit>()
    private val notificationCallbacks = mutableMapOf<Int, (Boolean) -> Unit>()

    private val storageLaunchers = mutableMapOf<Int, ActivityResultLauncher<String>>()
    private val storageCallbacks = mutableMapOf<Int, (Boolean) -> Unit>()

    /**
     * Must be called in Activity's onCreate()
     */
    fun initialize(activity: ComponentActivity) {
        // Initialize location permission launcher with callback
        locationLaunchers[activity.hashCode()] = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            showPermissionToast(activity, isGranted, "Location")
            locationCallbacks[activity.hashCode()]?.invoke(isGranted)
            locationCallbacks.remove(activity.hashCode())
        }

        // Initialize notification permission launcher with callback
        notificationLaunchers[activity.hashCode()] = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            showPermissionToast(activity, isGranted, "Notification")
            notificationCallbacks[activity.hashCode()]?.invoke(isGranted)
            notificationCallbacks.remove(activity.hashCode())
        }

        // Initialize camera permission launcher with callback
        cameraLaunchers[activity.hashCode()] = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            showPermissionToast(activity, isGranted, "Camera")
            cameraCallbacks[activity.hashCode()]?.invoke(isGranted)
            cameraCallbacks.remove(activity.hashCode())
        }

        // Add this to initialize function
        storageLaunchers[activity.hashCode()] = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            showPermissionToast(activity, isGranted, "Storage")
            storageCallbacks[activity.hashCode()]?.invoke(isGranted)
            storageCallbacks.remove(activity.hashCode())
        }
    }

    /**
     * Clean up when activity is destroyed
     */
    fun cleanup(activity: ComponentActivity) {
        val hashCode = activity.hashCode()
        cameraLaunchers.remove(hashCode)
        notificationLaunchers.remove(hashCode)
        locationLaunchers.remove(hashCode)
        cameraCallbacks.remove(hashCode)
        notificationCallbacks.remove(hashCode)
        locationCallbacks.remove(hashCode)
        storageLaunchers.remove(hashCode)
        storageCallbacks.remove(hashCode)
    }

    fun checkAndRequestLocationPermission(context: Context, callback: (Boolean) -> Unit) {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                callback(true)
            }
            context is Fragment -> {
                val launcher = context.registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    Log.d("check_location_permission","is location permission -> ${isGranted}")
                    showPermissionToast(context.requireContext(), isGranted, "Location")
                    callback(isGranted)
                }
                launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            context is ComponentActivity -> {
                // Store callback before launching
                locationCallbacks[context.hashCode()] = callback
                locationLaunchers[context.hashCode()]?.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    ?: run {
                        Log.d("check_location_permission","launcher not available")
                        // If launcher not available, clean up and deny
                        locationCallbacks.remove(context.hashCode())
                        callback(false)
                    }
            }
            else -> callback(false)
        }
    }


    fun checkAndRequestNotificationPermission(context: Context, callback: (Boolean) -> Unit) {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                callback(true)
            }
            context is Fragment -> {
                // Use fragment-specific launcher
                val launcher = context.registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    showPermissionToast(context.requireContext(), isGranted, "Notification")
                    callback(isGranted)
                }
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            context is ComponentActivity -> {
                // Use pre-registered launcher
                notificationLaunchers[context.hashCode()]?.launch(Manifest.permission.POST_NOTIFICATIONS)
                    ?: callback(false)
            }
            else -> callback(false)
        }
    }

    fun checkAndRequestCameraPermission(context: Context, callback: (Boolean) -> Unit) {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                callback(true)
            }
            context is Fragment -> {
                // Use fragment-specific launcher
                val launcher = context.registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    showPermissionToast(context.requireContext(), isGranted, "Camera")
                    callback(isGranted)
                }
                launcher.launch(Manifest.permission.CAMERA)
            }
            context is ComponentActivity -> {
                // Use pre-registered launcher
                cameraLaunchers[context.hashCode()]?.launch(Manifest.permission.CAMERA)
                    ?: callback(false)
            }
            else -> callback(false)
        }
    }

    private fun showPermissionToast(context: Context, granted: Boolean, permissionType: String) {
        Toast.makeText(
            context,
            if (granted) "$permissionType Permission Granted" else "$permissionType Permission Denied",
            Toast.LENGTH_SHORT
        ).show()
    }

    // Add this new function
    fun checkAndRequestStoragePermission(context: Context, callback: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10+, no permission needed for Downloads directory
            callback(true)
        } else {
            when {
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                    callback(true)
                }
                context is Fragment -> {
                    val launcher = context.registerForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        showPermissionToast(context.requireContext(), isGranted, "Storage")
                        callback(isGranted)
                    }
                    launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                context is ComponentActivity -> {
                    storageCallbacks[context.hashCode()] = callback
                    storageLaunchers[context.hashCode()]?.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        ?: run {
                            storageCallbacks.remove(context.hashCode())
                            callback(false)
                        }
                }
                else -> callback(false)
            }
        }
    }
}