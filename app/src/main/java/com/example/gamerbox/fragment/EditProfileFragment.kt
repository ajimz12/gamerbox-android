package com.example.gamerbox.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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

    private var imageUri: Uri? = null

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
        val view = inflater.inflate(R.layout.activity_editprofile, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        profileImage = view.findViewById(R.id.profileImageView)
        usernameEditText = view.findViewById(R.id.usernameEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        updateButton = view.findViewById(R.id.updateButton)

        updateButton.setOnClickListener { updateProfile() }

        loadUserData()

        profileImage.setOnClickListener { pickImage() }

        return view
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val username = document.getString("username")
                        val email = document.getString("email")
                        val imageUrl = document.getString("imageUrl")

                        usernameEditText.setText(username)
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
                        Log.e("EditProfileFragment", "Error uploading image", e)
                    }
            } ?: run {
                updateUserProfile(userId, userData)
            }
        }
    }

    private fun updateUserProfile(userId: String, userData: Map<String, Any>) {
        db.collection("users").document(userId)
            .update(userData)
            .addOnSuccessListener {
                Log.d("EditProfileFragment", "User profile updated successfully")
                // Navigate back to ProfileFragment
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Log.e("EditProfileFragment", "Error updating user profile", e)
            }
    }
}
