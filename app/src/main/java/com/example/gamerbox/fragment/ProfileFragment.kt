package com.example.gamerbox.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.gamerbox.activity.AuthActivity
import com.example.gamerbox.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var profileImage: ImageView
    private lateinit var usernameText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_profile, container, false)
        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit_profile -> {
                    // Navegar a EditProfileFragment
                    findNavController().navigate(R.id.action_profile_to_edit_profile)
                    true
                }
                R.id.action_logout -> {
                    showLogoutConfirmationDialog()
                    true
                }
                else -> false
            }
        }

        profileImage = view.findViewById(R.id.profileImage)
        usernameText = view.findViewById(R.id.usernameText)

        // Mostrar los datos del usuario
        showUserData()

        return view
    }

    private fun showUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val username = document.getString("username")
                        val imageUrl = document.getString("imageUrl")

                        // Mostrar el nombre de usuario
                        usernameText.text = username

                        // Cargar la imagen del usuario con Glide
                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(imageUrl)
                                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                                .into(profileImage)

                        } else {
                            // Si no hay URL de imagen, mostrar una imagen de placeholder
                            Glide.with(this)
                                .load(R.drawable.ic_profile)
                                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                                .into(profileImage)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Error
                }
        }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Cerrar sesión")
        builder.setMessage("¿Estás seguro de que quieres cerrar sesión?")
        builder.setPositiveButton("Aceptar") { _, _ ->
            // Cerrar la sesión del usuario
            auth.signOut()
            // Mostrar la pantalla de inicio de sesión
            showLogin()
        }
        builder.setNegativeButton("Cancelar", null)
        val dialog = builder.create()
        dialog.show()
    }

    private fun showLogin() {
        val loginIntent = Intent(requireContext(), AuthActivity::class.java)
        startActivity(loginIntent)
    }
}
