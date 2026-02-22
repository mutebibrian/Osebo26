package com.devbrian.osebo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel() {

    protected val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    protected val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    protected val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> get() = _success

    protected fun handleError(throwable: Throwable) {
        _error.postValue(throwable.message ?: "An error occurred")
        _loading.postValue(false)
    }
}