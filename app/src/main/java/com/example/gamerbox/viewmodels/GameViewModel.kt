package com.example.gamerbox.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamerbox.models.GameList
import com.example.gamerbox.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameViewModel: ViewModel() {

    private val _games = MutableStateFlow<List<GameList>>(emptyList())
    val games = _games.asStateFlow()

    init {
        getGames()
    }

    private fun getGames(){
        viewModelScope.launch(Dispatchers.IO){
            withContext(Dispatchers.Main){
                val response = RetrofitClient.retrofit.getGames()
                _games.value = response.body()?.gameList ?: emptyList()
            }
        }
    }
}