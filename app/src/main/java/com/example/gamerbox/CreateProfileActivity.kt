package com.example.gamerbox

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class CreateProfileActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var usernameEditText: EditText
    private lateinit var chooseImageButton: Button
    private lateinit var selectedImageView: ImageView
    private lateinit var continueButton: Button

    private var imageUri: Uri? = null

    // Activity Result Launcher for picking an image
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                selectedImageView.setImageURI(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acivity_createprofile)

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

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid

                        val user = hashMapOf(
                            "username" to username,
                            "email" to email,
                            "password" to password
                            // Add more fields as needed
                        )

                        userId?.let { uid ->
                            // If an image is selected, upload it to Firebase Storage
                            imageUri?.let { uri ->
                                val storageRef =
                                    FirebaseStorage.getInstance().reference.child("profile_images")
                                        .child("$uid.jpg")
                                storageRef.putFile(uri)
                                    .addOnSuccessListener { _ ->
                                        storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                            // Save the image URL to Firestore
                                            user["imageUrl"] = downloadUri.toString()

                                            saveUserDataToFirestore(userId, user)
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Storage", "Error uploading image", e)
                                        saveUserDataToFirestore(userId, user)
                                    }
                            } ?: run {
                                // If no image is selected, save user data to Firestore directly
                                saveUserDataToFirestore(userId, user)
                            }
                        }
                    } else {
                        // Error creating user in Firebase Authentication
                        Log.e("Auth", "Error creating user", task.exception)
                    }
                }
        }
    }

    private fun pickImage() {
        pickImageLauncher.launch("image/*")
    }

    private fun saveUserDataToFirestore(userId: String, userData: Map<String, Any>) {
        db.collection("users").document(userId).set(userData)
            .addOnSuccessListener {
                showHome()
            }
            .addOnFailureListener { e ->
                // Error saving user details to Firestore
                Log.e("Firestore", "Error saving user details", e)
            }
    }

    private fun showHome() {
        val homeIntent = Intent(this, MainActivity::class.java)
        startActivity(homeIntent)
        finish()
    }
}
