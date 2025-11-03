package com.ramankumar.moviefinder.ui.detail

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.ramankumar.moviefinder.R
import com.ramankumar.moviefinder.api.ApiConfig
import com.ramankumar.moviefinder.api.RetrofitClient
import com.ramankumar.moviefinder.data.local.AppDatabase
import com.ramankumar.moviefinder.data.repository.MovieRepository
import kotlinx.coroutines.launch

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var backdropImageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var yearTextView: TextView
    private lateinit var ratingTextView: TextView
    private lateinit var overviewTextView: TextView
    private lateinit var favoriteButton: MaterialButton

    private var movieId = 0
    private lateinit var repository: MovieRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

        val database = AppDatabase.getDatabase(applicationContext)
        repository = MovieRepository(
            movieDao = database.movieDao(),
            swipeHistoryDao = database.swipeHistoryDao(),
            favoriteDao = database.favoriteDao(),
            api = RetrofitClient.api,
            apiKey = ApiConfig.API_KEY
        )

        initializeViews()
        displayMovieData()
    }

    private fun initializeViews() {
        backdropImageView = findViewById(R.id.backdropImageView)
        titleTextView = findViewById(R.id.titleTextView)
        yearTextView = findViewById(R.id.yearTextView)
        ratingTextView = findViewById(R.id.ratingTextView)
        overviewTextView = findViewById(R.id.overviewTextView)
        favoriteButton = findViewById(R.id.favoriteButton)
    }

    private fun displayMovieData() {
        movieId = intent.getIntExtra("MOVIE_ID", 0)
        val movieTitle = intent.getStringExtra("MOVIE_TITLE") ?: ""
        val movieYear = intent.getStringExtra("MOVIE_YEAR") ?: ""
        val movieRating = intent.getDoubleExtra("MOVIE_RATING", 0.0)
        val movieOverview = intent.getStringExtra("MOVIE_OVERVIEW") ?: ""
        val movieBackdrop = intent.getStringExtra("MOVIE_BACKDROP") ?: ""

        titleTextView.text = movieTitle
        yearTextView.text = movieYear
        ratingTextView.text = "⭐ ${String.format("%.1f", movieRating)}/10"
        overviewTextView.text = movieOverview

        Glide.with(this)
            .load("https://image.tmdb.org/t/p/w780$movieBackdrop")
            .placeholder(R.drawable.ic_launcher_background)
            .into(backdropImageView)

        lifecycleScope.launch {
            val isFavorite = repository.isFavorite(movieId)
            updateFavoriteButton(isFavorite)
        }

        favoriteButton.setOnClickListener {
            toggleFavorite()
        }
    }

    private fun toggleFavorite() {
        lifecycleScope.launch {
            val newStatus = repository.toggleFavorite(movieId)
            updateFavoriteButton(newStatus)

            val message = if (newStatus) "Added to favorites" else "Removed from favorites"
            Toast.makeText(this@MovieDetailActivity, message, Toast.LENGTH_SHORT).show()

            val resultIntent = android.content.Intent()
            resultIntent.putExtra("MOVIE_ID", movieId)
            resultIntent.putExtra("IS_FAVORITE", newStatus)
            setResult(RESULT_OK, resultIntent)
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        if (isFavorite) {
            favoriteButton.text = "Remove from Favorites"
            favoriteButton.setIconResource(android.R.drawable.btn_star_big_on)
        } else {
            favoriteButton.text = "Add to Favorites"
            favoriteButton.setIconResource(android.R.drawable.btn_star_big_off)
        }
    }
}