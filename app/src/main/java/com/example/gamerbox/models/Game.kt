package com.example.gamerbox.models

import com.google.gson.annotations.SerializedName

data class Game(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("background_image") val backgroundImageUrl: String,
)



