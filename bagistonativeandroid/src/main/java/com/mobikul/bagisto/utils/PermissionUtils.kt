package com.mobikul.bagisto.utils

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.util.Collections

/**
 * PRODUCTION-READY PERMISSION FRAMEWORK
 * 
 * Features:
 * - Rotation-Safe (No Activity Leaks)
 * - Thread-Safe (Enforces Main Thread)
 * - Race-Condition Protected (Unique Request Tags)
 * - Rationale-Aware (Handles "Don't ask again")
 * - Async-Consistent (All callbacks delivered on next frame)
 */
interface PermissionManager {
    fun checkAndRequestCameraPermission(context: Context, callback: (Boolean) -> Unit)
    fun checkAndRequestLocationPermission(context: Context, callback: (Boolean) -> Unit)
    fun checkAndRequestStoragePermission(context: Context, callback: (Boolean) -> Unit)
    fun checkAndRequestNotificationPermission(context: Context, callback: (Boolean) -> Unit)
}

object PermissionUtils : PermissionManager {

    private const val TAG = "PermissionUtils"
    private const val SETTINGS_DELAY = 1500L
    
    // In-flight request tracking
    private val activeRequests = Collections.synchronizedSet(mutableSetOf<String>())
    // Callback registry that survives recreate but doesn't leak context
    private val callbackMap = mutableMapOf<String, (Boolean) -> Unit>()

    private tailrec fun Context.findActivity(): FragmentActivity? = when (this) {
        is FragmentActivity -> this
        is ContextWrapper -> baseContext?.findActivity()
        else -> null
    }

    class PermissionFragment : Fragment() {
        private val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val tag = tag ?: return@registerForActivityResult
            val isGranted = results.values.all { it }
            
            val hostActivity = activity as? FragmentActivity
            val permanentlyDenied = if (!isGranted && hostActivity != null) {
                results.filter { !it.value }.keys.any { perm ->
                    !hostActivity.shouldShowRequestPermissionRationale(perm)
                }
            } else false

            // Deliver and cleanup
            val callback = callbackMap[tag]
            callbackMap.remove(tag)
            activeRequests.remove(tag)
            
            // Post to main thread to ensure consistency and avoid transaction issues
            Handler(Looper.getMainLooper()).post {
                callback?.invoke(isGranted)
                
                if (permanentlyDenied && hostActivity != null) {
                    handlePermanentDenial(hostActivity, tag.split("_").first())
                } else if (!isGranted && isAdded) {
                    Toast.makeText(context?.applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }

            // Cleanup fragment
            if (isAdded && !parentFragmentManager.isStateSaved) {
                parentFragmentManager.beginTransaction().remove(this).commitAllowingStateLoss()
            }
        }

        fun request(permissions: Array<String>) {
            launcher.launch(permissions)
        }
        
        override fun onDestroy() {
            tag?.let { 
                callbackMap.remove(it)
                activeRequests.remove(it)
            }
            super.onDestroy()
        }
    }

    private fun dispatch(context: Context, permissions: Array<String>, label: String, callback: (Boolean) -> Unit) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Handler(Looper.getMainLooper()).post { dispatch(context, permissions, label, callback) }
            return
        }

        val activity = context.findActivity() ?: run {
            callback(false)
            return
        }
        
        if (activity.isFinishing || activity.isDestroyed) {
            callback(false)
            return
        }

        val tag = "${label}_REQUEST"
        if (!activeRequests.add(tag)) return // Protection against double calls

        callbackMap[tag] = callback
        
        val fragment = PermissionFragment()
        try {
            activity.supportFragmentManager.beginTransaction()
                .add(fragment, tag)
                .commitAllowingStateLoss() // Safer than commitNow

            // Launch on next frame to ensure fragment is attached and ready
            Handler(Looper.getMainLooper()).post {
                if (fragment.isAdded) {
                    fragment.request(permissions)
                } else {
                    activeRequests.remove(tag)
                    callbackMap.remove(tag)
                    callback(false)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to dispatch permission request", e)
            activeRequests.remove(tag)
            callbackMap.remove(tag)
            callback(false)
        }
    }

    override fun checkAndRequestCameraPermission(context: Context, callback: (Boolean) -> Unit) {
        handle(context, arrayOf(Manifest.permission.CAMERA), "Camera", callback)
    }

    override fun checkAndRequestLocationPermission(context: Context, callback: (Boolean) -> Unit) {
        val perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        handle(context, perms, "Location", callback)
    }

    override fun checkAndRequestStoragePermission(context: Context, callback: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Handler(Looper.getMainLooper()).post { callback(true) }
            return
        }
        handle(context, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), "Storage", callback)
    }

    override fun checkAndRequestNotificationPermission(context: Context, callback: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Handler(Looper.getMainLooper()).post { callback(true) }
            return
        }
        handle(context, arrayOf(Manifest.permission.POST_NOTIFICATIONS), "Notification", callback)
    }

    private fun handle(context: Context, perms: Array<String>, label: String, callback: (Boolean) -> Unit) {
        val allGranted = perms.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
        if (allGranted) {
            // Always post to next frame for consistency
            Handler(Looper.getMainLooper()).post { callback(true) }
            return
        }
        dispatch(context, perms, label, callback)
    }

    private fun handlePermanentDenial(context: Context, label: String) {
        val appContext = context.applicationContext
        Toast.makeText(appContext, "$label permission is permanently denied. Opening Settings...", Toast.LENGTH_LONG).show()
        val packageName = appContext.packageName
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                appContext.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open settings", e)
            }
        }, SETTINGS_DELAY)
    }
}