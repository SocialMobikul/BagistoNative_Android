package com.mobikul.bagisto.view_model

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

/**
 * Shared ViewModel for managing cart state across the application.
 * 
 * This ViewModel provides cart count state management that can be
 * shared between fragments and activities.
 * 
 * Features:
 * - Cart count state
 * - Cart count updates via bridge events
 * 
 * @property cartCount Current cart item count state
 * 
 * @see androidx.lifecycle.ViewModel
 * 
 * Usage:
 * ```kotlin
 * val viewModel: SharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
 * val count = viewModel.cartCount.value
 * viewModel.updateCartCount(5)
 * ```
 */
class SharedViewModel : ViewModel() {

    private val _cartCount = mutableStateOf(0)
    val cartCount: State<Int> = _cartCount

    /**
     * Update cart item count.
     * 
     * Changes the cart count and triggers recomposition in
     * Compose components observing this state.
     * 
     * @param newCount The new cart item count
     */
    fun updateCartCount(newCount: Int) {
        _cartCount.value = newCount
    }
}
