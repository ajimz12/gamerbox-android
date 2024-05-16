package com.example.gamerbox.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.gamerbox.R
import com.example.gamerbox.models.Review
import com.google.firebase.firestore.FirebaseFirestore

class ReviewAdapter : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    private var reviewsList: List<Review> = emptyList()

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reviewTextView: TextView = itemView.findViewById(R.id.reviewTextView)
        val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        val userProfileImageView: ImageView = itemView.findViewById(R.id.userProfileImageView)
        val ratingBar: RatingBar = itemView.findViewById(R.id.reviewRatingBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewsList[position]
        holder.reviewTextView.text = review.reviewText
        holder.ratingBar.rating = review.rating

        val userId = review.userId
        val userProfileRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        userProfileRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot != null && documentSnapshot.exists()) {
                val userName = documentSnapshot.getString("username")
                val userProfileImageUrl = documentSnapshot.getString("imageUrl")
                holder.userNameTextView.text = userName
                Glide.with(holder.userProfileImageView.context)
                    .load(userProfileImageUrl)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .into(holder.userProfileImageView)
            }
        }
    }

    override fun getItemCount(): Int {
        return reviewsList.size
    }

    fun submitList(reviews: List<Review>) {
        reviewsList = reviews
        notifyDataSetChanged()
    }
}
