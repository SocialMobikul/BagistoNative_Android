package com.mobikul.bagisto.components

import android.os.Build
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import kotlinx.serialization.Serializable

/**
 * Bridge component for haptic feedback (vibration) on Android devices.
 * 
 * This component enables the web layer to trigger haptic feedback
 * using Android's vibration API. Supports different feedback types
 * for success, warning, and error scenarios.
 * 
 * @property name The bridge component name used in web calls
 * @property bridgeDelegate Delegate for handling bridge communication
 * 
 * @see BridgeComponent
 * @see android.view.HapticFeedbackConstants
 * 
 * Requirements:
 * - Android 11 (API 30) or later
 * 
 * Usage from JavaScript:
 * ```javascript
 * // Success feedback
 * window.BagistoNative.haptic.vibrate({ feedback: 'success' });
 * 
 * // Warning feedback
 * window.BagistoNative.haptic.vibrate({ feedback: 'warning' });
 * 
 * // Error feedback
 * window.BagistoNative.haptic.vibrate({ feedback: 'error' });
 * ```
 * 
 * @constructor
 * @param name Component identifier for the bridge
 * @param bridgeDelegate Bridge delegate for message handling
 */
class HapticComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val fragment: Fragment
        get() = bridgeDelegate.destination.fragment

    /**
     * Handle incoming messages from the web layer.
     * 
     * Processes "vibrate" event to trigger haptic feedback.
     * 
     * @param message The incoming message from web layer
     * 
     * @see Message
     */
    override fun onReceive(message: Message) {
        when (message.event) {
            "vibrate" -> handleVibrateEvent(message)
            else -> Log.w("HapticComponent", "Unknown event for message: $message")
        }
    }

    /**
     * Handle vibrate event with version check.
     * 
     * Checks Android version and delegates to vibrate function
     * if Android 11+ is available.
     * 
     * @param message The message containing feedback type
     * 
     * @see Build.VERSION_CODES.R
     */
    private fun handleVibrateEvent(message: Message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            vibrate(message)
        } else {
            Log.e("HapticComponent", "Access to the haptics engine requires Android 11 or later.")
        }
    }

    /**
     * Trigger haptic feedback based on feedback type.
     * 
     * Uses HapticFeedbackConstants to provide different feedback patterns:
     * - "success": CONFIRM feedback
     * - "warning" or "error": REJECT feedback
     * 
     * @param message The message containing feedback configuration
     * 
     * @see HapticFeedbackConstants
     */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun vibrate(message: Message) {
        val data = message.data<MessageData>() ?: return

        when (data.feedback) {
            "success" -> fragment.view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            "warning" -> fragment.view?.performHapticFeedback(HapticFeedbackConstants.REJECT)
            "error" -> fragment.view?.performHapticFeedback(HapticFeedbackConstants.REJECT)
            else -> fragment.view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        }
    }

    /**
     * Data class for haptic feedback configuration.
     * 
     * @property feedback The feedback type: "success", "warning", or "error"
     */
    @Serializable
    data class MessageData(
        val feedback: String
    )
}
