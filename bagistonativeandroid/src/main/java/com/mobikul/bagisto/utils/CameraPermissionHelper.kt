package com.mobikul.bagisto.utils

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * Utility class for handling camera permission requests.
 * 
 * This helper provides a simplified API for requesting camera
 * permissions required by barcode scanner and image search features.
 * 
 * Features:
 * - Runtime permission checking
 * - Permission request handling
 * - Activity result API integration
 * 
 * @property activity Host activity for permission requests
 * @property onPermissionResult Callback for permission result
 * 
 * @see android.Manifest.permission#CAMERA
 * 
 * @constructor
 * @param activity Host activity for permission requests
 * @param onPermissionResult Callback for permission result
 */
class CameraPermissionHelper(private val fragment: Fragment) {
    private var onPermissionGranted: (() -> Unit)? = null
    private var onPermissionDenied: (() -> Unit)? = null

    private val requestPermissionLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted?.invoke()
        } else {
            onPermissionDenied?.invoke()
            showPermissionDeniedToast(fragment.requireContext())
        }
    }

    /**
     * Check and request camera permission.
     * 
     * Checks current permission status and requests permission if needed.
     * Calls appropriate callback based on permission result.
     * 
     * @param onGranted Callback invoked when permission is granted
     * @param onDenied Callback invoked when permission is denied
     */
    fun checkAndRequestCameraPermission(
        onGranted: () -> Unit,
        onDenied: () -> Unit = {}
    ) {
        onPermissionGranted = onGranted
        onPermissionDenied = onDenied

        when {
            ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                onGranted()
            }
            fragment.shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> {
                showPermissionRationale(fragment.requireContext())
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    /**
     * Show permission rationale toast.
     * 
     * Informs user why camera permission is needed.
     * 
     * @param context Context for showing toast
     */
    private fun showPermissionRationale(context: Context) {
        Toast.makeText(
            context,
            "Camera permission is required for product scanning",
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Show permission denied toast.
     * 
     * Informs user that feature is inaccessible without permission.
     * 
     * @param context Context for showing toast
     */
    private fun showPermissionDeniedToast(context: Context) {
        Toast.makeText(
            context,
            "Without camera permission this feature is not accessible",
            Toast.LENGTH_LONG
        ).show()
    }

    companion object {
        /**
         * Check if camera permission is granted.
         * 
         * @param context Context to check permission in
         * @return true if camera permission is granted
         */
        fun hasCameraPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}