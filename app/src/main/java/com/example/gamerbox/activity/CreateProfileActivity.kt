package com.example.gamerbox.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.gamerbox.R
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
    private lateinit var errorTextView: TextView

    private var imageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                Glide.with(this)
                    .load(it)
                    .circleCrop()
                    .into(selectedImageView)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createprofile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        usernameEditText = findViewById(R.id.usernameEditText)
        chooseImageButton = findViewById(R.id.chooseImageButton)
        selectedImageView = findViewById(R.id.selectedImageView)
        continueButton = findViewById(R.id.continueButton)
        errorTextView = findViewById(R.id.errorTextView)

        chooseImageButton.setOnClickListener {
            pickImage()
        }

        continueButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            if (username.isEmpty()) {
                errorTextView.text = getString(R.string.empty_username_error)
                return@setOnClickListener
            }

            checkUsernameExists(username) { exists ->
                if (exists) {
                    errorTextView.text = getString(R.string.usermame_already_exists_error)
                } else {
                    createUser(username)
                }
            }
        }
    }

    private fun pickImage() {
        pickImageLauncher.launch("image/*")
    }

    private fun checkUsernameExists(username: String, callback: (Boolean) -> Unit) {
        db.collection("users").whereEqualTo("username", username).get()
            .addOnSuccessListener { documents ->
                callback(!documents.isEmpty)
            }
    }

    private fun createUser(username: String) {
        val email = intent.getStringExtra("email") ?: ""
        val password = intent.getStringExtra("password") ?: ""

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid

                        val user = hashMapOf(
                            "username" to username,
                            "email" to email
                        )

                        userId?.let { uid ->
                            imageUri?.let { uri ->
                                val storage = FirebaseStorage.getInstance().reference.child("profile_images/$uid")
                                storage.putFile(uri).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        storage.downloadUrl.addOnSuccessListener { downloadUri ->
                                            user["imageUrl"] = downloadUri.toString()
                                            saveUserDataToFirestore(uid, user)
                                        }
                                    } else {
                                        saveUserDataToFirestore(uid, user)
                                    }
                                }
                            } ?: run {
                                saveUserDataToFirestore(uid, user)
                            }
                        }
                    } else {
                        errorTextView.text = task.exception?.message
                    }
                }
                .addOnFailureListener { _ ->
                    errorTextView.text = getString(R.string.user_already_exists_error)
                }
        } else {
            errorTextView.text = getString(R.string.auth_error)
        }
    }

    private fun saveUserDataToFirestore(userId: String, userData: Map<String, Any>) {
        db.collection("users").document(userId).set(userData)
            .addOnSuccessListener {
                showHome()
            }
            .addOnFailureListener { e ->
                errorTextView.text = e.message
            }
    }

    private fun showHome() {
        val homeIntent = Intent(this, MainActivity::class.java)
        startActivity(homeIntent)
        finish()
    }
}
