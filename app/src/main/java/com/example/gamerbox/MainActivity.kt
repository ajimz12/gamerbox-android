package com.example.gamerbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.gamerbox.fragment.HomeFragment
import com.example.gamerbox.fragment.ProfileFragment
import com.example.gamerbox.fragment.SearchFragment
import com.example.gamerbox.navigation.NavManager
import com.example.gamerbox.ui.theme.GamerboxTheme
import com.example.gamerbox.viewmodels.GameViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.analytics.FirebaseAnalytics

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar los fragments
        val homeFragment = HomeFragment()
        val searchFragment = SearchFragment()
        val profileFragment = ProfileFragment()

        // Mostrar el fragment inicial
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.content, homeFragment)
            commit()
        }

        // Manejar la selección de ítems en el BottomNavigationView
        val bottomNav = findViewById<BottomNavigationView>(R.id.navigator)
        bottomNav.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.searchPage -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.content, searchFragment)
                        commit()
                    }
                    true
                }
                R.id.homePage -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.content, homeFragment)
                        commit()
                    }
                    true
                }
                R.id.profilePage -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.content, profileFragment)
                        commit()
                    }
                    true
                }
                else -> false
            }
        }
    }

        /*val viewModel: GameViewModel by viewModels()

        setContent {
            GamerboxTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavManager(viewModel = viewModel)
                }
            }
        }*/

    }






