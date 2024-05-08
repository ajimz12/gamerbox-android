package com.example.gamerbox.models

import com.google.gson.annotations.SerializedName

data class GameDetails(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("background_image") val backgroundImageUrl: String,
    @SerializedName("released") val releaseDate: String,
    @SerializedName("description") val description: String,
    @SerializedName("background_image_additional") val backgroundAdditionalImageUrl: String,
    @SerializedName("metacritic") val metacritic: Int
)
