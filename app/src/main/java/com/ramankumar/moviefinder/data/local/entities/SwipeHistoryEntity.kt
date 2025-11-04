package com.ramankumar.moviefinder.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "swipe_history")
data class SwipeHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val movieId: Int,
    val movieTitle: String,
    val swipedRight: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)