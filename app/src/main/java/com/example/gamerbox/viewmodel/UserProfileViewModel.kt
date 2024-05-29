package com.example.gamerbox.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamerbox.models.User
import com.example.gamerbox.repository.UserRepository
import kotlinx.coroutines.launch

class UserProfileViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> get() = _userProfile

    private val _loading = MutableLiveData<Boolean>()
    fun fetchUserProfile(userId: String) {
        _loading.value = true
        viewModelScope.launch {
            val profile = userRepository.getUserProfile(userId)
            _userProfile.value = profile
            _loading.value = false
        }
    }
}
