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
import com.example.gamerbox.models.Game
import com.example.gamerbox.models.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReviewAdapter(
    private var reviewList: List<Review>,
    private val fromFragment: String,
    private val onLikeClicked: (Review) -> Unit
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reviewTextView: TextView = itemView.findViewById(R.id.reviewTextView)
        val ratingBar: RatingBar = itemView.findViewById(R.id.reviewRatingBar)
        val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        val userProfileImageView: ImageView = itemView.findViewById(R.id.userProfileImageView)
        val likeButton: ImageView = itemView.findViewById(R.id.likeButton)
        val likeCountTextView: TextView = itemView.findViewById(R.id.likeCountTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]
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

                "ProfileFragment" -> holder.itemView.findNavController()
                    .navigate(R.id.action_profile_to_reviewFragment, bundle)

                "UserReviewsFragment" -> holder.itemView.findNavController()
                    .navigate(R.id.action_userReviewsFragment_to_reviewFragment, bundle)
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

        holder.likeCountTextView.text = review.likes.size.toString()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val isLiked = review.likes.contains(currentUserId)
        holder.likeButton.setImageResource(
            if (isLiked) R.drawable.ic_heart_selected else R.drawable.ic_heart
        )

        holder.likeButton.setOnClickListener {
            onLikeClicked(review)
        }

    }

    override fun getItemCount(): Int {
        return reviewList.size
    }

    private fun truncateText(text: String, maxLength: Int): String {
        return if (text.length > maxLength) {
            "${text.substring(0, maxLength)}..."
        } else {
            text
        }
    }

    fun updateReview(updatedReview: Review) {
        val index = reviewList.indexOfFirst { it.id == updatedReview.id }
        if (index != -1) {
            reviewList = reviewList.toMutableList().apply {
                set(index, updatedReview)
            }
            notifyItemChanged(index)
        }
    }

    fun updateData(newReviewList: List<Review>) {
        reviewList = newReviewList
        notifyDataSetChanged()
    }
}
