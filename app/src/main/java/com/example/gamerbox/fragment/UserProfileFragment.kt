package com.example.gamerbox.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.gamerbox.R

class UserProfileFragment : Fragment() {

    private lateinit var userProfileImageView: ImageView
    private lateinit var userNameTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userProfileImageView = view.findViewById(R.id.userProfileImage)
        userNameTextView = view.findViewById(R.id.usernameProfileTextView)

        val userId = arguments?.getString("userId")
        val userProfileImageUrl = arguments?.getString("imageUrl")

        userId?.let {
            userNameTextView.text = it

            userProfileImageUrl?.let { imageUrl ->
                Glide.with(userProfileImageView.context)
                    .load(imageUrl)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .into(userProfileImageView)
            } ?: run {
                userProfileImageView.setImageResource(R.drawable.ic_profile)
            }
        }
    }
}
