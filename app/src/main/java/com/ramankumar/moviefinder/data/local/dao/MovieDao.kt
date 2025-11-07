package com.ramankumar.moviefinder.data.local.dao

import androidx.room.*
import com.ramankumar.moviefinder.data.local.entities.MovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: Int): MovieEntity?

    @Query("SELECT * FROM movies ORDER BY cachedTimestamp DESC LIMIT 20")
    fun getPopularMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies ORDER BY cachedTimestamp DESC")
    suspend fun getAllMovies(): List<MovieEntity>

    @Query("DELETE FROM movies")
    suspend fun deleteAllMovies()

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%'")
    fun searchMovies(query: String): Flow<List<MovieEntity>>

    @Query("DELETE FROM movies WHERE cachedTimestamp < :timestamp")
    suspend fun deleteOldCache(timestamp: Long)

    @Query("SELECT COUNT(*) FROM movies")
    suspend fun getMovieCount(): Int
}