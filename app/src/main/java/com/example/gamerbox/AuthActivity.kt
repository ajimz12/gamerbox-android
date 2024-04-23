package com.example.gamerbox

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthActivity : ComponentActivity() {

    lateinit var loginButton: Button
    lateinit var registerButton: Button
    lateinit var emailEditText: EditText
    lateinit var passwordEditText: EditText
    lateinit var passwordErrorTextView: TextView

    // Instancia de FirebaseAuth
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Instancia de FirebaseFirestore
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            if (emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()) {
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()

                // Iniciar la actividad de registro y pasar el correo electrónico y la contraseña
                val intent = Intent(this, EditProfileActivity::class.java).apply {
                    putExtra("email", email)
                    putExtra("password", password)
                }
                startActivity(intent)
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
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val password = s.toString()
                    if (password.length < 6) {
                        // Mostrar el mensaje de error
                        passwordErrorTextView.visibility = View.VISIBLE
                        passwordErrorTextView.text =
                            "La contraseña debe tener al menos 6 caracteres"
                    } else {
                        // Ocultar el mensaje de error
                        passwordErrorTextView.visibility = View.GONE
                    }
                }
            })
        }

        private fun createUserDocument(userId: String?) {
            userId?.let {
                // Creamos un nuevo documento de usuario con el ID de usuario como identificador
                val userDocument = firestore.collection("user").document(userId)

                // Define los datos que deseas almacenar para el nuevo usuario
                val userData = hashMapOf(
                    "email" to emailEditText.text.toString(),
                    "password" to passwordEditText.text.toString() // TODO: Cifrar contraseña a BD
                )

                // Escribe los datos del nuevo usuario en el documento de usuario recién creado
                userDocument.set(userData)
                    .addOnSuccessListener {

                    }
                    .addOnFailureListener { e ->
                        // Ocurrió un error al intentar crear el documento de usuario
                    }
            }
        }

        private fun showAlert() {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Ups!")
            builder.setMessage("Se ha producido un error autenticando al usuario")
            builder.setPositiveButton("Aceptar", null)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        private fun showHome() {
            val homeIntent = Intent(this, MainActivity::class.java)
            startActivity(homeIntent)
            finish()
        }
    }

