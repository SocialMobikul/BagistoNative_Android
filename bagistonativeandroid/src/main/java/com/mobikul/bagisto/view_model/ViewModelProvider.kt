package com.mobikul.bagisto.view_model

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

/**
 * Custom ViewModelProvider singleton for Bagisto Native SDK.
 * 
 * This provider provides application-specific ViewModels with
 * proper lifecycle management across the SDK.
 * 
 * Features:
 * - Initialize with ViewModelStoreOwner
 * - Factory support for AndroidViewModel
 * - Type-safe ViewModel retrieval
 * 
 * @property viewModelStore Store for ViewModel instances
 * @property factory Factory for ViewModel creation
 * 
 * @see androidx.lifecycle.ViewModelProvider
 * @see SharedViewModel
 * 
 * Usage:
 * ```kotlin
 * // Initialize in Application or Activity
 * ViewModelProvider.initialize(this, application)
 * 
 * // Get ViewModel
 * val sharedViewModel = ViewModelProvider.get(SharedViewModel::class.java)
 * ```
 */
object ViewModelProvider {
    private var viewModelStore: ViewModelStore? = null
    private var factory: ViewModelProvider.Factory = ViewModelProvider.NewInstanceFactory()

    /**
     * Initialize the ViewModelProvider.
     * 
     * Must be called once before using get() method. Sets up the
     * ViewModelStore and factory for creating ViewModels.
     * 
     * @param owner The ViewModelStoreOwner (Fragment or Activity)
     * @param application Optional Application for AndroidViewModel support
     */
    fun initialize(owner: ViewModelStoreOwner, application: Application? = null) {
        viewModelStore = owner.viewModelStore
        factory = if (application != null) {
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        } else {
            ViewModelProvider.NewInstanceFactory()
        }
    }

    /**
     * Get a ViewModel instance.
     * 
     * Returns an existing ViewModel or creates a new one if not found.
     * 
     * @param T The ViewModel class type
     * @param modelClass The class of the ViewModel to retrieve
     * @return The ViewModel instance
     * @throws IllegalStateException if initialize() hasn't been called
     */
    fun <T : ViewModel> get(modelClass: Class<T>): T {
        return ViewModelProvider(
            viewModelStore ?: throw IllegalStateException("ViewModelProvider not initialized"),
            factory
        ).get(modelClass)
    }
}
