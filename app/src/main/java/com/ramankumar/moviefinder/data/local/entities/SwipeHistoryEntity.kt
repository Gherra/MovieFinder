package com.ramankumar.moviefinder.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "swipe_history")
data class SwipeHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val movieId: Int,
    val movieTitle: String,
    val swipedRight: Boolean,  // true = liked (swipe right)
    val swipedNeutral: Boolean = false,  // true = not sure (swipe up)
    val timestamp: Long = System.currentTimeMillis()
)