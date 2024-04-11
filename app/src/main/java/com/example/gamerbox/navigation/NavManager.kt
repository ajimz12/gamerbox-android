package com.example.gamerbox.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gamerbox.viewmodels.GameViewModel
import com.example.gamerbox.views.StartView

@Composable
fun NavManager(viewModel: GameViewModel) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "start"
    ){
        composable("start"){
            StartView(navController, viewModel)
        }
    }
}