package com.ramankumar.moviefinder.data.repository

import com.ramankumar.moviefinder.api.TMDbApi
import com.ramankumar.moviefinder.data.local.dao.FavoriteDao
import com.ramankumar.moviefinder.data.local.dao.MovieDao
import com.ramankumar.moviefinder.data.local.dao.SwipeHistoryDao
import com.ramankumar.moviefinder.data.local.entities.FavoriteEntity
import com.ramankumar.moviefinder.data.local.entities.SwipeHistoryEntity
import com.ramankumar.moviefinder.data.local.entities.toEntity
import com.ramankumar.moviefinder.data.local.entities.toMovie
import com.ramankumar.moviefinder.model.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import com.ramankumar.moviefinder.data.local.entities.toEntity
import com.ramankumar.moviefinder.data.local.entities.toMovie

class MovieRepository(
    private val movieDao: MovieDao,
    private val swipeHistoryDao: SwipeHistoryDao,
    private val favoriteDao: FavoriteDao,
    private val api: TMDbApi,
    private val apiKey: String
) {

    suspend fun getPopularMovies(forceRefresh: Boolean = false): Result<List<Movie>> {
        return try {
            if (forceRefresh || movieDao.getMovieCount() == 0) {
                // Fetch from API
                val response = api.getPopularMovies(apiKey, page = 1)
                val movies = response.body()?.results ?: emptyList()

                // Cache in database
                movieDao.deleteAllMovies()
                movieDao.insertMovies(movies.map { it.toEntity() })

                Result.success(movies)
            } else {
                // Return from cache
                val cachedEntities = movieDao.getAllMovies()
                val cachedMovies = cachedEntities.map { it.toMovie() }
                Result.success(cachedMovies)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchMovies(query: String): Result<List<Movie>> {
        return try {
            val response = api.searchMovies(apiKey, query)
            val movies = response.body()?.results ?: emptyList()  // ← ADD .body()
            Result.success(movies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordSwipe(movie: Movie, liked: Boolean) {
        val swipe = SwipeHistoryEntity(
            movieId = movie.id,
            movieTitle = movie.title,
            swipedRight = liked
        )
        swipeHistoryDao.insertSwipe(swipe)
        movieDao.insertMovie(movie.toEntity())
    }

    fun getLikedMovies(): Flow<List<SwipeHistoryEntity>> {
        return swipeHistoryDao.getLikedMovies()
    }

    fun getDislikedMovies(): Flow<List<SwipeHistoryEntity>> {
        return swipeHistoryDao.getDislikedMovies()
    }

    suspend fun getSwipeStats(): SwipeStats {
        return SwipeStats(
            totalSwipes = swipeHistoryDao.getTotalSwipeCount(),
            liked = swipeHistoryDao.getLikedCount(),
            disliked = swipeHistoryDao.getDislikedCount()
        )
    }

    suspend fun addFavorite(movieId: Int) {
        favoriteDao.addFavorite(FavoriteEntity(movieId))
    }

    suspend fun removeFavorite(movieId: Int) {
        favoriteDao.removeFavorite(movieId)
    }

    suspend fun toggleFavorite(movieId: Int): Boolean {
        return if (favoriteDao.isFavorite(movieId)) {
            favoriteDao.removeFavorite(movieId)
            false
        } else {
            favoriteDao.addFavorite(FavoriteEntity(movieId))
            true
        }
    }

    fun getAllFavorites(): Flow<List<Movie>> {
        return favoriteDao.getAllFavorites().map { favorites ->
            favorites.mapNotNull { favorite ->
                movieDao.getMovieById(favorite.movieId)?.toMovie()?.apply {
                    isFavorite = true
                }
            }
        }
    }

    suspend fun isFavorite(movieId: Int): Boolean {
        return favoriteDao.isFavorite(movieId)
    }

    suspend fun getShuffledMovies(): Result<List<Movie>> {
        return try {
            android.util.Log.d("MovieRepository", "getShuffledMovies: Starting API call")

            val response = api.getPopularMovies(apiKey, page = 1)
            android.util.Log.d("MovieRepository", "getShuffledMovies: API response code = ${response.code()}")

            val movies = response.body()?.results ?: emptyList()
            android.util.Log.d("MovieRepository", "getShuffledMovies: Got ${movies.size} movies")

            if (movies.isEmpty()) {
                android.util.Log.e("MovieRepository", "getShuffledMovies: No movies found!")
                return Result.failure(Exception("No movies found"))
            }

            val shuffledMovies = movies.shuffled()
            android.util.Log.d("MovieRepository", "getShuffledMovies: Shuffled! Returning ${shuffledMovies.size} movies")

            Result.success(shuffledMovies)
        } catch (e: Exception) {
            android.util.Log.e("MovieRepository", "getShuffledMovies: Exception - ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getTopRatedMovies(): Result<List<Movie>> {
        return try {
            val response = api.getTopRatedMovies(apiKey, page = 1)
            val movies = response.body()?.results ?: emptyList()
            Result.success(movies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNowPlayingMovies(): Result<List<Movie>> {
        return try {
            val response = api.getNowPlayingMovies(apiKey, page = 1)
            val movies = response.body()?.results ?: emptyList()
            Result.success(movies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}

data class SwipeStats(
    val totalSwipes: Int,
    val liked: Int,
    val disliked: Int
)