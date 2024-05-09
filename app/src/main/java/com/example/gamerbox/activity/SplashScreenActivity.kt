package com.example.gamerbox.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.gamerbox.R

class SplashScreenActivity : AppCompatActivity() {

    private val SPLASH_SCREEN_TIMEOUT = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Retraso de transición a la siguiente actividad
        Handler(Looper.getMainLooper()).postDelayed({
            // Iniciar la siguiente actividad
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, SPLASH_SCREEN_TIMEOUT)
    }
}