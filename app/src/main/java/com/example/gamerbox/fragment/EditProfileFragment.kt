package com.example.gamerbox.fragment

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.gamerbox.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var profileImage: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var updateButton: Button
    private lateinit var chooseImageButton: Button

    private var imageUri: Uri? = null
    private var currentUsername: String? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            Glide.with(requireContext())
                .load(it)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(profileImage)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_editprofile, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        profileImage = view.findViewById(R.id.profileImageView)
        usernameEditText = view.findViewById(R.id.usernameEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        updateButton = view.findViewById(R.id.updateButton)
        chooseImageButton = view.findViewById(R.id.chooseImageButton)

        updateButton.setOnClickListener { updateProfile() }

        loadUserData()

        chooseImageButton.setOnClickListener { pickImage() }

        return view
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        currentUsername = document.getString("username")
                        val email = document.getString("email")
                        val imageUrl = document.getString("imageUrl")

                        usernameEditText.setText(currentUsername)
                        emailEditText.setText(email)

                        imageUrl?.let {
                            Glide.with(requireContext())
                                .load(imageUrl)
                                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                                .into(profileImage)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("EditProfileFragment", "Error loading user data", e)
                }
        }
    }

    private fun pickImage() {
        pickImageLauncher.launch("image/*")
    }

    private fun updateProfile() {
        val userId = auth.currentUser?.uid
        val newUsername = usernameEditText.text.toString().trim()

        if (userId != null) {
            // Check if the new username is different from the current one
            if (newUsername == currentUsername) {
                // Update only the image or other data if the username has not changed
                val userData = mutableMapOf<String, Any>("username" to newUsername)

                imageUri?.let { uri ->
                    val imageRef = storage.reference.child("profile_images").child("$userId.jpg")
                    imageRef.putFile(uri)
                        .addOnSuccessListener {
                            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                userData["imageUrl"] = downloadUri.toString()
                                updateUserProfile(userId, userData)
                            }
                        }
                        .addOnFailureListener { e ->
                            println(e.message)
                        }
                } ?: run {
                    updateUserProfile(userId, userData)
                }
            } else {
                // Check if the new username already exists
                checkIfUsernameExists(newUsername) { exists ->
                    if (exists) {
                        Toast.makeText(requireContext(), "El nombre de usuario ya existe", Toast.LENGTH_SHORT).show()
                    } else if (usernameEditText.text.isBlank()) {
                        Toast.makeText(requireContext(), "El nombre de usuario no puede estar vacío", Toast.LENGTH_SHORT).show()
                    } else {
                        val userData = mutableMapOf<String, Any>("username" to newUsername)

                        imageUri?.let { uri ->
                            val imageRef = storage.reference.child("profile_images").child("$userId.jpg")
                            imageRef.putFile(uri)
                                .addOnSuccessListener {
                                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                        userData["imageUrl"] = downloadUri.toString()
                                        updateUserProfile(userId, userData)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    println(e.message)
                                }
                        } ?: run {
                            updateUserProfile(userId, userData)
                        }
                    }
                }
            }
        }
    }

    private fun checkIfUsernameExists(username: String, callback: (Boolean) -> Unit) {
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    callback(false)
                } else {
                    callback(true)
                }
            }
            .addOnFailureListener { e ->
                println(e.message)
                callback(false)
            }
    }

    private fun updateUserProfile(userId: String, userData: Map<String, Any>) {
        db.collection("users").document(userId)
            .update(userData)
            .addOnSuccessListener {
                Log.d("EditProfileFragment", "User profile updated successfully")
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Log.e("EditProfileFragment", "Error updating user profile", e)
            }
    }
}
