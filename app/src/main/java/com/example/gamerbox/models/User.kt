package com.example.gamerbox.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User (
    @PrimaryKey(autoGenerate = true)
    val id: String,
    val username: String,
    val password: String,
    val email: String,
    val imageUrl: String,
    val favoriteGames: MutableList<Int>
)
{
    constructor() : this("","","","","", mutableListOf())
}