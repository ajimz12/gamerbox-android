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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.gamerbox.R
import com.example.gamerbox.activity.AuthActivity
import com.example.gamerbox.adapter.ReviewAdapter
import com.example.gamerbox.models.Review
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var profileImage: ImageView
    private lateinit var usernameText: TextView
    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var moreReviewsTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reviewRecyclerView = view.findViewById(R.id.profileReviewRecyclerView)
        moreReviewsTextView = view.findViewById(R.id.moreReviewsTextView)

        reviewRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadUserReviews()

        moreReviewsTextView.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_userReviews)
        }
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

    private fun loadUserReviews() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("reviews")
            .whereEqualTo("userId", userId)
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val reviewList = mutableListOf<Review>()
                for (document in documents) {
                    val review = document.toObject(Review::class.java)
                    reviewList.add(review)
                }
                if (reviewList.isNotEmpty()) {
                    val recentReviews = reviewList.take(3)
                    reviewAdapter = ReviewAdapter(recentReviews, "ProfileFragment")
                    reviewRecyclerView.adapter = reviewAdapter

                    if (reviewList.size > 3) {
                        moreReviewsTextView.visibility = View.VISIBLE
                    }
                }
            }
            .addOnFailureListener { exception ->
                println("Error al recibir documentos de BD: $exception")
            }
    }
}
