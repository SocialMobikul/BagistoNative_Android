package com.mobikul.bagisto.components

import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination

/**
 * Bridge component for Google Play in-app review prompts.
 * 
 * This component enables the web layer to trigger Google's
 * in-app review flow using the Play Core library. This allows
 * users to leave reviews without leaving the app.
 * 
 * @property name The bridge component name used in web calls
 * @property bridgeDelegate Delegate for handling bridge communication
 * 
 * @see BridgeComponent
 * @see com.google.android.play.core.review.ReviewManager
 * 
 * Requirements:
 * - Google Play Services installed
 * - Play Core library
 * 
 * Usage from JavaScript:
 * ```javascript
 * window.BagistoNative.review-prompt.prompt();
 * ```
 * 
 * Note: The review prompt may not always be shown (Google limits
 * the number of times a user can be prompted).
 * 
 * @constructor
 * @param name Component identifier for the bridge
 * @param bridgeDelegate Bridge delegate for message handling
 */
class ReviewPromptComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val fragment: Fragment
        get() = bridgeDelegate.destination.fragment

    private val manager: ReviewManager? by lazy {
        fragment.context?.let {
            ReviewManagerFactory.create(it)
        }
    }

    /**
     * Handle incoming messages from the web layer.
     * 
     * Processes "prompt" event to launch in-app review.
     * 
     * @param message The incoming message from web layer
     * 
     * @see Message
     */
    override fun onReceive(message: Message) {
        when (message.event) {
            "prompt" -> promptForReview()
            else -> Log.w("ReviewPromptComponent", "Unknown event for message: $message")
        }
    }

    /**
     * Launch Google Play in-app review flow.
     * 
     * Requests a review flow from Play Core, then launches
     * the review dialog. Handles success and error cases.
     * 
     * @see ReviewManager
     * @see com.google.android.play.core.review.ReviewManager
     */
    private fun promptForReview() {
        val request = manager?.requestReviewFlow()
        request?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = fragment.activity?.let { manager?.launchReviewFlow(it, reviewInfo) }
                flow?.addOnCompleteListener { _ ->
                    Log.d("ReviewPromptComponent", "Fake review flow completed.")
                }
            } else {
                Log.e("ReviewPromptComponent", task.exception?.message ?: "(no message)")
            }
        }
    }
}
