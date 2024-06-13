package com.example.gamerbox.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.gamerbox.R
import com.example.gamerbox.activity.AuthActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var profileImage: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var updateButton: Button
    private lateinit var chooseImageButton: Button
    private lateinit var deleteAccountButton: TextView

    private var imageUri: Uri? = null
    private var currentUsername: String? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
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
        updateButton = view.findViewById(R.id.updateButton)
        chooseImageButton = view.findViewById(R.id.chooseImageButton)
        deleteAccountButton = view.findViewById(R.id.deleteAccountText)

        updateButton.setOnClickListener { updateProfile() }
        chooseImageButton.setOnClickListener { pickImage() }
        deleteAccountButton.setOnClickListener { showDeleteAccountConfirmationDialog() }

        loadUserData()

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
                        val imageUrl = document.getString("imageUrl")

                        usernameEditText.setText(currentUsername)

                        imageUrl?.let {
                            Glide.with(requireContext())
                                .load(imageUrl)
                                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                                .into(profileImage)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    println(e.message)
                }
        }
    }

    private fun pickImage() {
        pickImageLauncher.launch("image/*")
    }

    private fun updateProfile() {
        val userId = auth.currentUser?.uid ?: return
        val newUsername = usernameEditText.text.toString().trim()

        if (newUsername == currentUsername) {
            val userData = mutableMapOf<String, Any>("username" to newUsername)
            handleProfileImageUpload(userId, userData)
        } else {
            if (newUsername.isBlank()) {
                showToast(R.string.empty_username_error)
            } else {
                checkIfUsernameExists(newUsername) { exists ->
                    if (exists) {
                        showToast(R.string.user_already_exists_error)
                    } else {
                        val userData = mutableMapOf<String, Any>("username" to newUsername)
                        handleProfileImageUpload(userId, userData)
                    }
                }
            }
        }
    }

    private fun handleProfileImageUpload(userId: String, userData: MutableMap<String, Any>) {
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

    private fun showToast(messageResId: Int) {
        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show()
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
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                println(e.message)
            }
    }

    private fun showDeleteAccountConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.delete_account)
        builder.setMessage(R.string.confirm_delete_owner)
        builder.setPositiveButton(R.string.confirm_text) { dialog, _ ->
            deleteAccount()
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.cancel_text) { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun deleteAccount() {
        val userId = auth.currentUser?.uid
        userId?.let {
            db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener {
                    db.collection("reviews").whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                db.collection("reviews").document(document.id).delete()
                            }
                            auth.currentUser?.delete()
                                ?.addOnSuccessListener {
                                    Toast.makeText(
                                        requireContext(),
                                        R.string.user_deleted_text,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    showLogin()
                                }
                        }
                        .addOnFailureListener { _ ->
                            Toast.makeText(
                                requireContext(),
                                "Error al eliminar las reseñas",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener { _ ->
                    Toast.makeText(
                        requireContext(),
                        "Error al eliminar el documento del usuario",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun showLogin() {
        val loginIntent = Intent(requireContext(), AuthActivity::class.java)
        startActivity(loginIntent)
    }
}
