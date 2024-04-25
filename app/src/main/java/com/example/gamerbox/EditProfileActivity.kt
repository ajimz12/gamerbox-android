package com.example.gamerbox

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var usernameEditText: EditText
    private lateinit var chooseImageButton: Button
    private lateinit var selectedImageView: ImageView
    private lateinit var continueButton: Button

    private var imageUri: Uri? = null

    // Activity Result Launcher for picking an image
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageView.setImageURI(it)
            imageUri = it
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acivity_editprofile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        usernameEditText = findViewById(R.id.usernameEditText)
        chooseImageButton = findViewById(R.id.chooseImageButton)
        selectedImageView = findViewById(R.id.selectedImageView)
        continueButton = findViewById(R.id.continueButton)

        // Choose Image Button Click Listener
        chooseImageButton.setOnClickListener {
            pickImage()
        }

        // Continue Button Click Listener
        continueButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = intent.getStringExtra("email") ?: ""
            val password = intent.getStringExtra("password") ?: ""

            if (username.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid

                            val user = hashMapOf(
                                "username" to username,
                                "email" to email,
                                "password" to password
                            )

                            if (userId != null) {
                                db.collection("users").document(userId).set(user)
                                    .addOnSuccessListener {
                                        showHome()
                                    }
                                    .addOnFailureListener { e ->
                                        // Error saving user details to Firestore
                                    }
                            }
                        } else {
                            // Error creating user in Firebase Authentication
                        }
                    }
            } else {
                // Username is empty
            }
        }
    }

    private fun pickImage() {
        pickImageLauncher.launch("image/*")
    }

    private fun showHome() {
        val homeIntent = Intent(this, MainActivity::class.java)
        startActivity(homeIntent)
        finish()
    }
}
