package com.mobikul.bagisto.components

import android.app.Activity
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.mobikul.bagisto.utils.AppSharedPreference
import com.mobikul.bagisto.utils.ThemeStateHolder
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.fragments.HotwireFragment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.json.JSONObject

class ThemeModeComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val TAG = "ThemeModeComponent"

    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

    override fun onReceive(message: Message) {
        Log.d(TAG, "Theme change requested: ${message.jsonData}")
        val theme = JSONObject(message.jsonData).optString("mode", "light")

        when (theme) {
            "light" -> {
                Log.d(TAG, "Setting Light Mode")
                AppSharedPreference.setDisplayTheme(false)
                ThemeStateHolder.updateTheme(false)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                updateUIColors(false)
            }
            "dark" -> {
                Log.d(TAG, "Setting Dark Mode")
                AppSharedPreference.setDisplayTheme(true)
                ThemeStateHolder.updateTheme(true)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                updateUIColors(true)
            }
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun updateUIColors(isDark: Boolean) {
        try {
            val activity = fragment.activity ?: return
            
            val surfaceColor = getThemeColor(activity, com.google.android.material.R.attr.colorSurface)
            val onSurfaceColor = getThemeColor(activity, com.google.android.material.R.attr.colorOnSurface)
            
            val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
            rootView?.let { updateViewsRecursively(it, surfaceColor, onSurfaceColor) }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update UI colors", e)
        }
    }

    private fun getThemeColor(activity: Activity, attr: Int): Int {
        val typedValue = TypedValue()
        activity.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    private fun updateViewsRecursively(view: View, backgroundColor: Int, iconColor: Int) {
        when (view) {
            is Toolbar -> {
                view.setBackgroundColor(backgroundColor)
                view.setTitleTextColor(iconColor)
                view.setSubtitleTextColor(iconColor)
                view.navigationIcon?.setTint(iconColor)
                view.overflowIcon?.setTint(iconColor)
                
                for (i in 0 until view.menu.size()) {
                    view.menu.getItem(i)?.icon?.setTint(iconColor)
                }
                
                for (i in 0 until view.childCount) {
                    updateViewsRecursively(view.getChildAt(i), backgroundColor, iconColor)
                }
            }
            is SearchView -> {
                tintSearchView(view, iconColor)
            }
            is ViewGroup -> {
                if (view.javaClass.name.contains("ComposeView")) {
                    view.invalidate()
                }
                for (i in 0 until view.childCount) {
                    updateViewsRecursively(view.getChildAt(i), backgroundColor, iconColor)
                }
            }
        }
    }

    private fun tintSearchView(searchView: SearchView, color: Int) {
        try {
            val searchIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
            searchIcon?.setColorFilter(color)

            val closeButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
            closeButton?.setColorFilter(color)

            val searchText = searchView.findViewById<TextView>(androidx.appcompat.R.id.search_src_text)
            searchText?.setTextColor(color)
            searchText?.setHintTextColor(color)

            val searchButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_button)
            searchButton?.setColorFilter(color)
            
            searchView.invalidate()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to tint SearchView", e)
        }
    }

    @Serializable
    private data class MessageData(val theme: Theme?)

    @Serializable
    private enum class Theme {
        @SerialName("light") LIGHT,
        @SerialName("dark") DARK
    }
}