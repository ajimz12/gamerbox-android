package com.example.gamerbox.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.gamerbox.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // NavController
        navController = findNavController(R.id.nav_host_fragment)

        // BottomNavigationView
        bottomNav = findViewById(R.id.navigator)
        bottomNav.setOnItemSelectedListener  { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }
                R.id.searchFragment -> {
                    navController.navigate(R.id.searchFragment)
                    true
                }
                R.id.profileFragment -> {
                    navController.navigate(R.id.profileFragment)
                    true
                }
                else -> false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.gameFragment) {
                // Ocultar el BottomNavigationView
                bottomNav.visibility = View.GONE
            } else {
                // Mostrar el BottomNavigationView
                bottomNav.visibility = View.VISIBLE
            }
        }
    }
}






