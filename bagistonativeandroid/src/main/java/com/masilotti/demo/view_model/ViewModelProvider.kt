package com.masilotti.demo.view_model

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

object ViewModelProvider {
    private var viewModelStore: ViewModelStore? = null
    private var factory: ViewModelProvider.Factory = ViewModelProvider.NewInstanceFactory()

    fun initialize(owner: ViewModelStoreOwner, application: Application? = null) {
        viewModelStore = owner.viewModelStore
        factory = if (application != null) {
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        } else {
            ViewModelProvider.NewInstanceFactory()
        }
    }

    fun <T : ViewModel> get(modelClass: Class<T>): T {
        return ViewModelProvider(
            viewModelStore ?: throw IllegalStateException("ViewModelProvider not initialized"),
            factory
        ).get(modelClass)
    }
}
