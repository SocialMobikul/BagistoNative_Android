package com.masilotti.demo.view_model

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    private val _cartCount = mutableStateOf(0)
    val cartCount: State<Int> = _cartCount

//    private val _isSearchVisible = mutableStateOf(false)
//    val isSearchVisible:State<Boolean> = _isSearchVisible

    fun updateCartCount(newCount: Int) {
        _cartCount.value = newCount
    }
}