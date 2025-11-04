package com.ramankumar.moviefinder.data.local.dao

import androidx.room.*
import com.ramankumar.moviefinder.data.local.entities.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE movieId = :movieId")
    suspend fun removeFavorite(movieId: Int)

    @Query("SELECT * FROM favorites ORDER BY addedTimestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE movieId = :movieId)")
    suspend fun isFavorite(movieId: Int): Boolean

    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun getFavoriteCount(): Int
}