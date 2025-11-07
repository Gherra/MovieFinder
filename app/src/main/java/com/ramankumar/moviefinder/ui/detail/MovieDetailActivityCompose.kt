package com.ramankumar.moviefinder.ui.detail

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.ramankumar.moviefinder.api.ApiConfig
import com.ramankumar.moviefinder.api.RetrofitClient
import com.ramankumar.moviefinder.data.local.AppDatabase
import com.ramankumar.moviefinder.data.repository.MovieRepository
import com.ramankumar.moviefinder.ui.compose.screens.DetailScreen
import com.ramankumar.moviefinder.ui.compose.theme.MovieFinderTheme
import kotlinx.coroutines.launch

class MovieDetailActivityCompose : ComponentActivity() {

    private lateinit var repository: MovieRepository
    private var movieId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get data from intent
        movieId = intent.getIntExtra("MOVIE_ID", 0)
        val movieTitle = intent.getStringExtra("MOVIE_TITLE") ?: ""
        val movieYear = intent.getStringExtra("MOVIE_YEAR") ?: ""
        val movieRating = intent.getDoubleExtra("MOVIE_RATING", 0.0)
        val movieOverview = intent.getStringExtra("MOVIE_OVERVIEW") ?: ""
        val movieBackdrop = intent.getStringExtra("MOVIE_BACKDROP")
        val initialIsFavorite = intent.getBooleanExtra("IS_FAVORITE", false)

        // Setup repository
        val database = AppDatabase.getDatabase(applicationContext)
        repository = MovieRepository(
            movieDao = database.movieDao(),
            swipeHistoryDao = database.swipeHistoryDao(),
            favoriteDao = database.favoriteDao(),
            api = RetrofitClient.api,
            apiKey = ApiConfig.API_KEY
        )

        setContent {
            MovieFinderTheme {
                DetailScreen(
                    movieId = movieId,
                    movieTitle = movieTitle,
                    movieYear = movieYear,
                    movieRating = movieRating,
                    movieOverview = movieOverview,
                    movieBackdrop = movieBackdrop,
                    initialIsFavorite = initialIsFavorite,
                    onBackClick = { finish() },
                    onFavoriteToggle = { newStatus ->
                        toggleFavorite(newStatus)
                    }
                )
            }
        }
    }

    private fun toggleFavorite(newStatus: Boolean) {
        lifecycleScope.launch {
            repository.toggleFavorite(movieId)

            val message = if (newStatus)
                "Added to favorites"
            else
                "Removed from favorites"

            Toast.makeText(
                this@MovieDetailActivityCompose,
                message,
                Toast.LENGTH_SHORT
            ).show()

            // Return result to MainActivity
            val resultIntent = Intent().apply {
                putExtra("MOVIE_ID", movieId)
                putExtra("IS_FAVORITE", newStatus)
            }
            setResult(RESULT_OK, resultIntent)
        }
    }
}