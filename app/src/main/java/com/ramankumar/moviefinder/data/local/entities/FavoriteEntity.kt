package com.ramankumar.moviefinder.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val movieId: Int,
    val addedTimestamp: Long = System.currentTimeMillis()
)