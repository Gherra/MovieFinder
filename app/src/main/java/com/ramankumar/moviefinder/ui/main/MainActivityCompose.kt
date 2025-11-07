package com.ramankumar.moviefinder.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.ramankumar.moviefinder.api.ApiConfig
import com.ramankumar.moviefinder.api.RetrofitClient
import com.ramankumar.moviefinder.data.local.AppDatabase
import com.ramankumar.moviefinder.data.repository.MovieRepository
import com.ramankumar.moviefinder.model.Movie
import com.ramankumar.moviefinder.ui.compose.screens.MainScreen
import com.ramankumar.moviefinder.ui.compose.theme.MovieFinderTheme
import com.ramankumar.moviefinder.ui.detail.MovieDetailActivityCompose
import com.ramankumar.moviefinder.ui.swipe.SwipeActivityCompose

class MainActivityCompose : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = MovieRepository(
            movieDao = database.movieDao(),
            swipeHistoryDao = database.swipeHistoryDao(),
            favoriteDao = database.favoriteDao(),
            api = RetrofitClient.api,
            apiKey = ApiConfig.API_KEY
        )
        MainViewModelFactory(repository)
    }

    private val detailActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Refresh movies after returning from detail
            viewModel.loadPopularMovies()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MovieFinderTheme {
                MainScreen(
                    viewModel = viewModel,
                    onMovieClick = { movie -> openMovieDetail(movie) },
                    onSwipeClick = { openSwipeActivity() }
                )
            }
        }
    }

    private fun openMovieDetail(movie: Movie) {
        val intent = Intent(this, MovieDetailActivityCompose::class.java).apply {
            putExtra("MOVIE_ID", movie.id)
            putExtra("MOVIE_TITLE", movie.title)
            putExtra("MOVIE_YEAR", movie.releaseDate.take(4))
            putExtra("MOVIE_RATING", movie.voteAverage)
            putExtra("MOVIE_OVERVIEW", movie.overview)
            putExtra("MOVIE_BACKDROP", movie.backdropPath)
            putExtra("IS_FAVORITE", movie.isFavorite)
        }
        detailActivityLauncher.launch(intent)
    }

    private fun openSwipeActivity() {
        val intent = Intent(this, SwipeActivityCompose::class.java)
        startActivity(intent)
    }
}