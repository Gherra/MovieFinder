package com.ramankumar.moviefinder.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ramankumar.moviefinder.api.ApiConfig
import com.ramankumar.moviefinder.api.GeminiService
import com.ramankumar.moviefinder.api.RetrofitClient
import com.ramankumar.moviefinder.data.local.AppDatabase
import com.ramankumar.moviefinder.data.repository.MovieRepository
import com.ramankumar.moviefinder.model.Movie
import com.ramankumar.moviefinder.model.auth.AuthResult
import com.ramankumar.moviefinder.ui.auth.AuthViewModel
import com.ramankumar.moviefinder.ui.compose.screens.MainScreen
import com.ramankumar.moviefinder.ui.compose.screens.auth.LoginScreen
import com.ramankumar.moviefinder.ui.compose.theme.MovieFinderTheme
import com.ramankumar.moviefinder.ui.detail.MovieDetailActivityCompose
import com.ramankumar.moviefinder.ui.swipe.SwipeActivityCompose

class MainActivityCompose : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    
    private val viewModel: MainViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val geminiService = GeminiService(ApiConfig.GEMINI_API_KEY)
        val repository = MovieRepository(
            movieDao = database.movieDao(),
            swipeHistoryDao = database.swipeHistoryDao(),
            favoriteDao = database.favoriteDao(),
            api = RetrofitClient.api,
            apiKey = ApiConfig.API_KEY,
            geminiService = geminiService
        )
        MainViewModelFactory(repository)
    }

    private val detailActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Refresh movies after returning from detail
            viewModel.loadTrendingMovies()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MovieFinderTheme {
                val authState by authViewModel.authState.collectAsState()
                
                // Show login screen if user is not authenticated
                when (authState) {
                    is AuthResult.Success -> {
                        // User is authenticated, show main screen
                        MainScreen(
                            viewModel = viewModel,
                            onMovieClick = { movie -> openMovieDetail(movie) },
                            onSwipeClick = { openSwipeActivity() },
                            onLogout = { authViewModel.logout() }
                        )
                    }
                    else -> {
                        // User is not authenticated, show login screen
                        LoginScreen(vm = authViewModel)
                    }
                }
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