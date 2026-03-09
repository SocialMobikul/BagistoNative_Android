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

interface PermissionManager {
    /**
     * Check and request camera permission.
     * 
     * @param context Android context
     * @param callback Callback with permission result (true if granted)
     */
    fun checkAndRequestCameraPermission(context: Context, callback: (Boolean) -> Unit)
    
    /**
     * Check and request location permissions (fine and coarse).
     * 
     * @param context Android context
     * @param callback Callback with permission result (true if granted)
     */
    fun checkAndRequestLocationPermission(context: Context, callback: (Boolean) -> Unit)
    
    /**
     * Check and request storage permission.
     * 
     * Automatically grants on Android 10+ (scoped storage).
     * 
     * @param context Android context
     * @param callback Callback with permission result (true if granted)
     */
    fun checkAndRequestStoragePermission(context: Context, callback: (Boolean) -> Unit)
    
    /**
     * Check and request notification permission.
     * 
     * Automatically grants on Android versions below 13.
     * 
     * @param context Android context
     * @param callback Callback with permission result (true if granted)
     */
    fun checkAndRequestNotificationPermission(context: Context, callback: (Boolean) -> Unit)
}

/**
 * Utility object for handling runtime permissions in Android.
 * 
 * This object provides a unified API for requesting common Android permissions
 * with proper handling of:
 * - Permission granted/denied callbacks
 * - Permanent denial handling
 * - Automatic settings redirect
 * - Thread safety for multiple requests
 * 
 * @see PermissionManager
 * @see Manifest.permission
 * 
 * Usage:
 * ```kotlin
 * PermissionUtils.checkAndRequestLocationPermission(context) { granted ->
 *     if (granted) {
 *         // Permission granted, proceed
 *     } else {
 *         // Permission denied
 *     }
 * }
 * ```
 * 
 * Note: Requires CAMERA, ACCESS_FINE_LOCATION, and other permissions
 * to be declared in AndroidManifest.xml
 */
object PermissionUtils : PermissionManager {

    private const val TAG = "PermissionUtils"
    private const val SETTINGS_DELAY = 1500L
    
    private val activeRequests = Collections.synchronizedSet(mutableSetOf<String>())
    private val callbackMap = mutableMapOf<String, (Boolean) -> Unit>()

    /**
     * Find the FragmentActivity from context.
     * 
     * Recursively traverses context hierarchy to find FragmentActivity.
     * 
     * @return FragmentActivity if found, null otherwise
     */
    private tailrec fun Context.findActivity(): FragmentActivity? = when (this) {
        is FragmentActivity -> this
        is ContextWrapper -> baseContext?.findActivity()
        else -> null
    }

    /**
     * Internal fragment for handling permission request results.
     * 
     * Uses Activity Result API to request permissions and handle results.
     * Automatically removes itself after permission result.
     */
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

            val callback = callbackMap[tag]
            callbackMap.remove(tag)
            activeRequests.remove(tag)
            
            Handler(Looper.getMainLooper()).post {
                callback?.invoke(isGranted)
                
                if (permanentlyDenied && hostActivity != null) {
                    handlePermanentDenial(hostActivity, tag.split("_").first())
                } else if (!isGranted && isAdded) {
                    Toast.makeText(context?.applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }

            if (isAdded && !parentFragmentManager.isStateSaved) {
                parentFragmentManager.beginTransaction().remove(this).commitAllowingStateLoss()
            }
        }

        /**
         * Request the specified permissions.
         * 
         * @param permissions Array of permission strings to request
         */
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

    /**
     * Dispatch permission request to the activity.
     * 
     * Creates a PermissionFragment, commits it, and requests permissions.
     * Handles thread safety and activity state checks.
     * 
     * @param context Android context
     * @param permissions Array of permissions to request
     * @param label Label for tracking this request
     * @param callback Callback with result
     */
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
        if (!activeRequests.add(tag)) return

        callbackMap[tag] = callback
        
        val fragment = PermissionFragment()
        try {
            activity.supportFragmentManager.beginTransaction()
                .add(fragment, tag)
                .commitAllowingStateLoss()

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

    /**
     * Check and request camera permission (CAMERA).
     * 
     * @param context Android context
     * @param callback Callback with permission result
     * 
     * @see Manifest.permission.CAMERA
     */
    override fun checkAndRequestCameraPermission(context: Context, callback: (Boolean) -> Unit) {
        handle(context, arrayOf(Manifest.permission.CAMERA), "Camera", callback)
    }

    /**
     * Check and request location permissions (ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION).
     * 
     * @param context Android context
     * @param callback Callback with permission result
     * 
     * @see Manifest.permission.ACCESS_FINE_LOCATION
     * @see Manifest.permission.ACCESS_COARSE_LOCATION
     */
    override fun checkAndRequestLocationPermission(context: Context, callback: (Boolean) -> Unit) {
        val perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        handle(context, perms, "Location", callback)
    }

    /**
     * Check and request storage permission (WRITE_EXTERNAL_STORAGE).
     * 
     * Automatically grants on Android 10+ due to scoped storage.
     * 
     * @param context Android context
     * @param callback Callback with permission result
     * 
     * @see Manifest.permission.WRITE_EXTERNAL_STORAGE
     */
    override fun checkAndRequestStoragePermission(context: Context, callback: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Handler(Looper.getMainLooper()).post { callback(true) }
            return
        }
        handle(context, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), "Storage", callback)
    }

    /**
     * Check and request notification permission (POST_NOTIFICATIONS).
     * 
     * Automatically grants on Android versions below 13 (TIRAMISU).
     * 
     * @param context Android context
     * @param callback Callback with permission result
     * 
     * @see Manifest.permission.POST_NOTIFICATIONS
     */
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
