package com.example.gamerbox.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gamerbox.components.CardGame
import com.example.gamerbox.components.MainTopBar
import com.example.gamerbox.viewmodels.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartView(navController: NavController, viewModel: GameViewModel) {

    Scaffold(
        topBar = {
            MainTopBar(title = "API JUEGOS")
        }
    ) {
        StartViewContent(
            navController = navController,
            viewModel = viewModel,
            pad = it
        )
    }
}

@Composable
fun StartViewContent(
    navController: NavController,
    viewModel: GameViewModel,
    pad: PaddingValues
) {
    val games by viewModel.games.collectAsState()
    
    LazyColumn(
        modifier = Modifier
            .padding(pad)
            .background(Color.Red)
    ){
        items(games){
            CardGame(game = it) {  }
            Text(
                text = it.name,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .padding(start = 12.dp)
            )
        }
    }


}