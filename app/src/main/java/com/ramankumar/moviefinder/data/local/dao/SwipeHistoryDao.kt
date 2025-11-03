package com.ramankumar.moviefinder.data.local.dao

import androidx.room.*
import com.ramankumar.moviefinder.data.local.entities.SwipeHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SwipeHistoryDao {

    @Insert
    suspend fun insertSwipe(swipe: SwipeHistoryEntity)

    @Query("SELECT * FROM swipe_history ORDER BY timestamp DESC")
    fun getAllSwipes(): Flow<List<SwipeHistoryEntity>>

    @Query("SELECT * FROM swipe_history WHERE swipedRight = 1 ORDER BY timestamp DESC")
    fun getLikedMovies(): Flow<List<SwipeHistoryEntity>>

    @Query("SELECT * FROM swipe_history WHERE swipedRight = 0 ORDER BY timestamp DESC")
    fun getDislikedMovies(): Flow<List<SwipeHistoryEntity>>

    @Query("SELECT COUNT(*) FROM swipe_history WHERE swipedRight = 1")
    suspend fun getLikedCount(): Int

    @Query("SELECT COUNT(*) FROM swipe_history WHERE swipedRight = 0")
    suspend fun getDislikedCount(): Int

    @Query("SELECT COUNT(*) FROM swipe_history")
    suspend fun getTotalSwipeCount(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM swipe_history WHERE movieId = :movieId)")
    suspend fun hasSwipedOn(movieId: Int): Boolean
}