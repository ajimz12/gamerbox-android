package com.example.gamerbox

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : ComponentActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var usernameEditText: EditText
    private lateinit var chooseImageButton: Button
    private lateinit var selectedImageView: ImageView
    private lateinit var continueButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acivity_editprofile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        usernameEditText = findViewById(R.id.usernameEditText)
        chooseImageButton = findViewById(R.id.chooseImageButton)
        selectedImageView = findViewById(R.id.selectedImageView)
        continueButton = findViewById(R.id.continueButton)

        // Clic en el botón de registro
        continueButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email =
                intent.getStringExtra("email") // Obtener el correo electrónico pasado desde AuthActivity
            val password =
                intent.getStringExtra("password") // Obtener la contraseña pasada desde AuthActivity

            // Verificar que el nombre de usuario no esté vacío
            if (username.isNotEmpty()) {
                // Crear el usuario en Firebase Authentication
                auth.createUserWithEmailAndPassword(email!!, password!!)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // El usuario se registró correctamente en Firebase Authentication
                            val userId = auth.currentUser?.uid

                            // Guardar los detalles adicionales del usuario en Firestore
                            val user = hashMapOf(
                                "username" to username,
                                // Agregar otros campos como la imagen del usuario si es necesario
                            )

                            // Agregar el documento del usuario a la colección "users"
                            if (userId != null) {
                                db.collection("users").document(userId).set(user)
                                    .addOnSuccessListener {
                                        showHome()
                                    }
                                    .addOnFailureListener { e ->
                                        // Error al guardar los detalles del usuario en Firestore
                                    }
                            }
                        } else {
                            // Error al crear el usuario en Firebase Authentication
                        }
                    }
            } else {
                // El nombre de usuario está vacío, mostrar un mensaje de error o realizar alguna acción
            }
        }
    }

    private fun showHome() {
        val homeIntent = Intent(this, MainActivity::class.java)
        startActivity(homeIntent)
        finish()
    }
}
