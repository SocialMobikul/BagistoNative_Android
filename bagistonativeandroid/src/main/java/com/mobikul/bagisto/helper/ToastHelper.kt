package com.mobikul.bagisto.helper

import android.content.Context
import android.widget.Toast

/**
 * Utility object for showing toast messages.
 * 
 * This helper provides a simplified API for displaying toast
 * messages from anywhere in the application.
 * 
 * @see android.widget.Toast
 * 
 * Usage:
 * ```kotlin
 * ToastHelper.show("Operation successful")
 * ToastHelper.show("Error: \${error.message}")
 * ```
 */
object ToastHelper {
    /**
     * Show a short duration toast message.
     * 
     * @param context The context to show toast in
     * @param message The message to display
     */
    fun showShortToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Show a long duration toast message.
     * 
     * @param context The context to show toast in
     * @param message The message to display
     */
    fun showLongToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}