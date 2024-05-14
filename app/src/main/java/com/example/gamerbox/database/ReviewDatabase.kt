package com.example.gamerbox.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.gamerbox.models.Review

@Database(entities = [Review::class], version = 1)
@TypeConverters(DateTypeConverter::class)
abstract class ReviewDatabase : RoomDatabase() {
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile private var instance: ReviewDatabase? = null

        fun getInstance(context: Context): ReviewDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ReviewDatabase::class.java,
                    "review_database"
                ).build().also { instance = it }
            }
        }
    }
}
