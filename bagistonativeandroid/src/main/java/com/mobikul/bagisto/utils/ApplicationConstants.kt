package com.mobikul.bagisto.utils

/**
 * Application-wide constants for Bagisto Native SDK.
 * 
 * This file contains string constants used across the SDK
 * for bridge component names, event names, and configuration keys.
 */
object ApplicationConstants {
    val ROOT_URL = "https://bagisto-native-commerce.vercel.app/"
    val DEFAULT_FCM_TOPICS = arrayOf("hotwire_mobikul_woocommerece")
    val BUNDLE_KEY_NOTIFICATION_ID = "from_notification"
    val NOTIFICATION_URL = "notification_url"
    val PRODUCT_URL = "product_url"
    val CATEGORY_URL = "category_url"
    val NOTIFICATION_TYPE = "type"
    val IS_FOREGROUND_NOTIFICATION = "isforground_notification"

    val LOGOUT_BUTTON_ID = 11
}