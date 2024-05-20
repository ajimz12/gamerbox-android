package com.example.gamerbox.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.gamerbox.R
import com.example.gamerbox.models.Review
import com.google.firebase.firestore.FirebaseFirestore

class ReviewAdapter(private val fromFragment: String) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    private var reviewsList: List<Review> = listOf()

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reviewTextView: TextView = itemView.findViewById(R.id.reviewTextView)
        val ratingBar: RatingBar = itemView.findViewById(R.id.reviewRatingBar)
        val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        val userProfileImageView: ImageView = itemView.findViewById(R.id.userProfileImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewsList[position]
        holder.reviewTextView.text = truncateText(review.reviewText, 100)
        holder.ratingBar.rating = review.rating

        val userId = review.userId
        val userProfileRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putString("reviewText", review.reviewText)
                putFloat("rating", review.rating)
                putLong("date", review.date.time)
                putString("userId", review.userId)
                putInt("gameId", review.gameId)
            }
            when (fromFragment) {
                "GameFragment" -> holder.itemView.findNavController()
                    .navigate(R.id.action_game_to_review, bundle)

                "AllReviewsFragment" -> holder.itemView.findNavController()
                    .navigate(R.id.action_allReviewsFragment_to_reviewFragment, bundle)
            }
        }

        userProfileRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot != null && documentSnapshot.exists()) {
                val userName = documentSnapshot.getString("username")
                holder.userNameTextView.text = userName

                val userProfileImageUrl = documentSnapshot.getString("imageUrl")
                if (!userProfileImageUrl.isNullOrEmpty()) {
                    Glide.with(holder.userProfileImageView.context)
                        .load(userProfileImageUrl)
                        .apply(RequestOptions.bitmapTransform(CircleCrop()))
                        .into(holder.userProfileImageView)
                } else {
                    Glide.with(holder.userProfileImageView.context)
                        .load(R.drawable.ic_profile)
                        .apply(RequestOptions.bitmapTransform(CircleCrop()))
                        .into(holder.userProfileImageView)
                }
            }
        }
    }

    override fun getItemCount() = reviewsList.size

    fun submitList(list: List<Review>) {
        reviewsList = list
        notifyDataSetChanged()
    }

    private fun truncateText(text: String, maxLength: Int): String {
        return if (text.length > maxLength) {
            "${text.substring(0, maxLength)}..."
        } else {
            text
        }
    }
}

