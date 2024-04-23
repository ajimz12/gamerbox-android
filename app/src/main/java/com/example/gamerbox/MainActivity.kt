package com.example.gamerbox

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gamerbox.fragment.HomeFragment
import com.example.gamerbox.fragment.ProfileFragment
import com.example.gamerbox.fragment.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

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






