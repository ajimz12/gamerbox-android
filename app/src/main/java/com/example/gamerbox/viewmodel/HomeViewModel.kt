package com.example.gamerbox.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamerbox.models.Game
import com.example.gamerbox.network.RawgRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val rawgRepository: RawgRepository) : ViewModel() {

    private val _popularGames = MutableLiveData<List<Game>>()
    val popularGames: LiveData<List<Game>> get() = _popularGames

    private val _recentGames = MutableLiveData<List<Game>>()
    val recentGames: LiveData<List<Game>> get() = _recentGames

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun fetchGames(apiKey: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val popularGames = rawgRepository.getPopularGames(apiKey)
                _popularGames.value = popularGames ?: emptyList()

                val recentGames = rawgRepository.getRecentPopularGames(apiKey)
                _recentGames.value = recentGames ?: emptyList()

            } catch (e: Exception) {
                println(e.message)

            } finally {
                _loading.value = false
            }
        }
    }
}
