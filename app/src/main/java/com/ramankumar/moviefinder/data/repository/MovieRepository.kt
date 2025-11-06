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
                val response = api.getPopularMovies(apiKey)
                val entities = response.results.map { it.toEntity() }
                movieDao.insertMovies(entities)
            }

            val movies = movieDao.getPopularMovies().first().map { entity ->
                val movie = entity.toMovie()
                movie.isFavorite = favoriteDao.isFavorite(movie.id)
                movie
            }

            Result.success(movies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchMovies(query: String): Result<List<Movie>> {
        return try {
            val response = api.searchMovies(apiKey, query)

            // not caching search results nw. didnt sevre any purpose
            val movies = response.results.map { movie ->
                movie.isFavorite = favoriteDao.isFavorite(movie.id)
                movie
            }

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
}

data class SwipeStats(
    val totalSwipes: Int,
    val liked: Int,
    val disliked: Int
)