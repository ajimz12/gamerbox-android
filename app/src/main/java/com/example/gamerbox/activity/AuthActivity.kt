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

class AuthActivity : ComponentActivity() {

    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordErrorTextView: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val homeIntent = Intent(this, MainActivity::class.java)
            startActivity(homeIntent)
            finish()
            return
        }

        setContentView(R.layout.activity_auth)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerbutton)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        passwordErrorTextView = findViewById(R.id.passwordErrorTextView)
        setUp()
    }

    private fun setUp() {
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (isEmailValid(email) && password.isNotEmpty()) {
                auth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val signInMethods = task.result.signInMethods
                        if (signInMethods != null && signInMethods.isNotEmpty()) {
                            showEmailAlreadyExistsAlert()
                        } else {
                            val intent = Intent(this, CreateProfileActivity::class.java).apply {
                                putExtra("email", email)
                                putExtra("password", password)
                            }
                            startActivity(intent)
                        }
                    } else {
                        showAlert(getString(R.string.auth_error))
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
                            showAlert(task.exception?.message ?: getString(R.string.auth_error))
                        }
                    }
            }
        }

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (password.length < 6) {
                    passwordErrorTextView.visibility = View.VISIBLE
                    passwordErrorTextView.text = getString(R.string.password_limit_text)
                } else {
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

    private fun showEmailAlreadyExistsAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.incorrect_email_header)
        builder.setMessage(R.string.incorrect_email_message)
        builder.setPositiveButton(R.string.accept_text, null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showAlert(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ups!")
        builder.setMessage(message)
        builder.setPositiveButton(R.string.accept_text, null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome() {
        val homeIntent = Intent(this, MainActivity::class.java)
        startActivity(homeIntent)
        finish()
    }
}
