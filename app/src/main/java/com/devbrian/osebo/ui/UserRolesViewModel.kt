package com.devbrian.osebo.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devbrian.osebo.models.UserRole

class UserRolesViewModel : ViewModel() {

    private val _userRoles = MutableLiveData<List<UserRole>>()
    val userRoles: LiveData<List<UserRole>> = _userRoles

    init {
        
        _userRoles.value = listOf(
            UserRole("Admin", "Full system access"),
            UserRole("Manager", "Manage shop operations"),
            UserRole("Cashier", "Process sales only")
        )
    }
}

