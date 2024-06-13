package com.example.gamerbox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gamerbox.network.RawgRepository

class HomeViewModelFactory(private val rawgRepository: RawgRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(rawgRepository) as T
        }
        throw IllegalArgumentException()
    }
}
