package com.example.gamerbox.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.gamerbox.R
import com.google.firebase.auth.FirebaseAuth
import org.mindrot.jbcrypt.BCrypt

class AuthActivity : ComponentActivity() {

    lateinit var loginButton: Button
    lateinit var registerButton: Button
    lateinit var emailEditText: EditText
    lateinit var passwordEditText: EditText
    lateinit var passwordErrorTextView: TextView

    // Instancia de FirebaseAuth
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Si hay un usuario autenticado, ir directamente a MainActivity
            val homeIntent = Intent(this, MainActivity::class.java)
            startActivity(homeIntent)

            // Finalizar la actividad actual
            finish()
            return
        }

        setContentView(R.layout.activity_auth)

        // Inicializar vistas después de que la actividad haya sido inflada
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerbutton)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        passwordErrorTextView = findViewById(R.id.passwordErrorTextView)
        setUp()

    }

    // Setup
    private fun setUp() {

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (isEmailValid(email) && password.isNotEmpty()) {
                // Intentar iniciar sesión para verificar si el usuario ya existe
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            showAlert()
                        } else {
                            // Encriptar contraseña
                            val encryptedPassword = encryptPassword(password)
                            val intent = Intent(this, CreateProfileActivity::class.java).apply {
                                putExtra("email", email)
                                putExtra("password", encryptedPassword)
                            }
                            startActivity(intent)
                        }
                    }
            } else {
                showInvalidEmailAlert()
            }
        }



        loginButton.setOnClickListener {
            if (emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()) {
                auth.signInWithEmailAndPassword(
                    emailEditText.text.toString(),
                    passwordEditText.text.toString()
                )
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            showHome()
                        } else {
                            showAlert()
                        }
                    }
            }
        }

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (password.length < 6) {
                    // Mostrar el mensaje de error
                    passwordErrorTextView.visibility = View.VISIBLE
                    passwordErrorTextView.text =
                        getString(R.string.password_limit_text)
                } else {
                    // Ocultar el mensaje de error
                    passwordErrorTextView.visibility = View.GONE
                }
            }
        })
    }

    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showInvalidEmailAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.incorrect_email_header)
        builder.setMessage(R.string.incorrect_email_message)
        builder.setPositiveButton(R.string.accept_text, null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ups!")
        builder.setMessage(R.string.auth_error)
        builder.setPositiveButton(R.string.accept_text, null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome() {
        val homeIntent = Intent(this, MainActivity::class.java)
        startActivity(homeIntent)
        finish()
    }

    private fun encryptPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }
}

