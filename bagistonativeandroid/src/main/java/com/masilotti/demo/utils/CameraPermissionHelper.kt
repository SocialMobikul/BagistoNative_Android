package com.masilotti.demo.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

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
                // Explain why you need permission
                showPermissionRationale(fragment.requireContext())
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
            else -> {
                // Directly request the permission
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    private fun showPermissionRationale(context: Context) {
        Toast.makeText(
            context,
            "Camera permission is required for product scanning",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showPermissionDeniedToast(context: Context) {
        Toast.makeText(
            context,
            "Without camera permission this feature is not accessible",
            Toast.LENGTH_LONG
        ).show()
    }

    companion object {
        fun hasCameraPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}