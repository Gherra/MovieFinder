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

class MovieRepository(
    private val movieDao: MovieDao,
    private val swipeHistoryDao: SwipeHistoryDao,
    private val favoriteDao: FavoriteDao,
    private val api: TMDbApi,
    private val apiKey: String
) {

    // ========== PAGINATION SUPPORT ==========

    /**
     * Fetches multiple pages of popular movies for pagination
     * @param startPage Starting page number (default 1)
     * @param pageCount Number of pages to fetch (default 5 = ~100 movies)
     * @param forceRefresh Whether to bypass cache and fetch fresh data
     */
    suspend fun getPopularMovies(
        startPage: Int = 1,
        pageCount: Int = 5,
        forceRefresh: Boolean = false
    ): Result<List<Movie>> {
        return try {
            if (forceRefresh || movieDao.getMovieCount() == 0) {
                // Fetch multiple pages from API
                val allMovies = mutableListOf<Movie>()

                for (page in startPage until startPage + pageCount) {
                    val response = api.getPopularMovies(apiKey, page = page)
                    val movies = response.body()?.results ?: emptyList()
                    allMovies.addAll(movies)
                    android.util.Log.d("MovieRepository", "getPopularMovies: Fetched page $page with ${movies.size} movies")
                }

                // Cache in database (only if startPage == 1 to avoid duplicates)
                if (startPage == 1) {
                    movieDao.deleteAllMovies()
                    movieDao.insertMovies(allMovies.map { it.toEntity() })
                }

                android.util.Log.d("MovieRepository", "getPopularMovies: Total movies fetched = ${allMovies.size}")
                Result.success(allMovies)
            } else {
                // Return from cache
                val cachedEntities = movieDao.getAllMovies()
                val cachedMovies = cachedEntities.map { it.toMovie() }
                Result.success(cachedMovies)
            }
        } catch (e: Exception) {
            android.util.Log.e("MovieRepository", "getPopularMovies: Error - ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Loads more pages for infinite scroll
     */
    suspend fun loadMorePopularMovies(currentPage: Int): Result<List<Movie>> {
        return getPopularMovies(startPage = currentPage, pageCount = 3, forceRefresh = true)
    }

    suspend fun getTopRatedMovies(
        startPage: Int = 1,
        pageCount: Int = 5
    ): Result<List<Movie>> {
        return try {
            val allMovies = mutableListOf<Movie>()

            for (page in startPage until startPage + pageCount) {
                val response = api.getTopRatedMovies(apiKey, page = page)
                val movies = response.body()?.results ?: emptyList()
                allMovies.addAll(movies)
            }

            android.util.Log.d("MovieRepository", "getTopRatedMovies: Total = ${allMovies.size}")
            Result.success(allMovies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNowPlayingMovies(
        startPage: Int = 1,
        pageCount: Int = 5
    ): Result<List<Movie>> {
        return try {
            val allMovies = mutableListOf<Movie>()

            for (page in startPage until startPage + pageCount) {
                val response = api.getNowPlayingMovies(apiKey, page = page)
                val movies = response.body()?.results ?: emptyList()
                allMovies.addAll(movies)
            }

            android.util.Log.d("MovieRepository", "getNowPlayingMovies: Total = ${allMovies.size}")
            Result.success(allMovies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== SWIPE FUNCTIONALITY ==========

    /**
     * Gets shuffled movies for swiping, excluding already swiped ones
     * Fetches 10 pages (~200 movies) for variety
     */
    suspend fun getShuffledMovies(): Result<List<Movie>> {
        return try {
            android.util.Log.d("MovieRepository", "getShuffledMovies: Starting API call")

            // Fetch multiple pages for variety (10 pages = ~200 movies)
            val allMovies = mutableListOf<Movie>()
            for (page in 1..10) {
                val response = api.getPopularMovies(apiKey, page = page)
                val movies = response.body()?.results ?: emptyList()
                allMovies.addAll(movies)
            }

            android.util.Log.d("MovieRepository", "getShuffledMovies: Fetched ${allMovies.size} movies from 10 pages")

            // Get list of already swiped movie IDs
            val swipedIds = swipeHistoryDao.getAllSwipedMovieIds()
            android.util.Log.d("MovieRepository", "getShuffledMovies: ${swipedIds.size} movies already swiped")

            // Filter out swiped movies
            val unswipedMovies = allMovies.filter { it.id !in swipedIds }
            android.util.Log.d("MovieRepository", "getShuffledMovies: ${unswipedMovies.size} unswiped movies available")

            if (unswipedMovies.isEmpty()) {
                android.util.Log.e("MovieRepository", "getShuffledMovies: No unswiped movies! Returning all movies.")
                // If all movies swiped, return all (user can reset history)
                val shuffled = allMovies.shuffled()
                return Result.success(shuffled)
            }

            val shuffledMovies = unswipedMovies.shuffled()
            android.util.Log.d("MovieRepository", "getShuffledMovies: Returning ${shuffledMovies.size} shuffled movies")

            Result.success(shuffledMovies)
        } catch (e: Exception) {
            android.util.Log.e("MovieRepository", "getShuffledMovies: Exception - ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Records a swipe action (left = dislike, right = like, up = neutral)
     * @param neutral Set to true for "not sure" swipes (swipe up gesture)
     */
    suspend fun recordSwipe(movie: Movie, liked: Boolean, neutral: Boolean = false) {
        val swipe = SwipeHistoryEntity(
            movieId = movie.id,
            movieTitle = movie.title,
            swipedRight = liked,
            swipedNeutral = neutral
        )
        swipeHistoryDao.insertSwipe(swipe)
        movieDao.insertMovie(movie.toEntity())

        android.util.Log.d("MovieRepository", "recordSwipe: Movie ${movie.id} - liked=$liked, neutral=$neutral")
    }

    /**
     * Clears all swipe history (for reset functionality)
     */
    suspend fun clearSwipeHistory() {
        swipeHistoryDao.deleteAllSwipes()
        android.util.Log.d("MovieRepository", "clearSwipeHistory: All swipe history cleared")
    }

    suspend fun getSwipeStats(): SwipeStats {
        return SwipeStats(
            totalSwipes = swipeHistoryDao.getTotalSwipeCount(),
            liked = swipeHistoryDao.getLikedCount(),
            disliked = swipeHistoryDao.getDislikedCount(),
            neutral = swipeHistoryDao.getNeutralCount()
        )
    }

    // ========== SEARCH ==========

    suspend fun searchMovies(query: String): Result<List<Movie>> {
        return try {
            // Search across multiple pages for better results
            val allMovies = mutableListOf<Movie>()

            for (page in 1..3) {
                val response = api.searchMovies(apiKey, query, page = page)
                val movies = response.body()?.results ?: emptyList()
                allMovies.addAll(movies)

                // Stop if we got less than 20 results (last page)
                if (movies.size < 20) break
            }

            Result.success(allMovies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== FAVORITES ==========

    fun getLikedMovies(): Flow<List<SwipeHistoryEntity>> {
        return swipeHistoryDao.getLikedMovies()
    }

    fun getDislikedMovies(): Flow<List<SwipeHistoryEntity>> {
        return swipeHistoryDao.getDislikedMovies()
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
}

data class SwipeStats(
    val totalSwipes: Int,
    val liked: Int,
    val disliked: Int,
    val neutral: Int = 0
)