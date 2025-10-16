package com.ramankumar.moviefinder

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var backdropImageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var yearTextView: TextView
    private lateinit var ratingTextView: TextView
    private lateinit var overviewTextView: TextView
    private lateinit var favoriteButton: MaterialButton

    private var isFavorite = false
    private var movieId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

        // Initialize views
        backdropImageView = findViewById(R.id.backdropImageView)
        titleTextView = findViewById(R.id.titleTextView)
        yearTextView = findViewById(R.id.yearTextView)
        ratingTextView = findViewById(R.id.ratingTextView)
        overviewTextView = findViewById(R.id.overviewTextView)
        favoriteButton = findViewById(R.id.favoriteButton)

        // Get movie data from intent
        movieId = intent.getIntExtra("MOVIE_ID", 0)
        val movieTitle = intent.getStringExtra("MOVIE_TITLE") ?: ""
        val movieYear = intent.getStringExtra("MOVIE_YEAR") ?: ""
        val movieRating = intent.getDoubleExtra("MOVIE_RATING", 0.0)
        val movieOverview = intent.getStringExtra("MOVIE_OVERVIEW") ?: ""
        val movieBackdrop = intent.getStringExtra("MOVIE_BACKDROP") ?: ""
        isFavorite = intent.getBooleanExtra("IS_FAVORITE", false)

        // Set data
        titleTextView.text = movieTitle
        yearTextView.text = movieYear
        ratingTextView.text = "⭐ ${String.format("%.1f", movieRating)}/10"
        overviewTextView.text = movieOverview

        // Load backdrop image
        Glide.with(this)
            .load("https://image.tmdb.org/t/p/w780$movieBackdrop")
            .placeholder(R.drawable.ic_launcher_background)
            .into(backdropImageView)

        updateFavoriteButton()

        // Favorite button click
        favoriteButton.setOnClickListener {
            isFavorite = !isFavorite
            updateFavoriteButton()

            // Send result back to MainActivity
            val resultIntent = android.content.Intent()
            resultIntent.putExtra("MOVIE_ID", movieId)
            resultIntent.putExtra("IS_FAVORITE", isFavorite)
            setResult(RESULT_OK, resultIntent)
        }
    }

    private fun updateFavoriteButton() {
        if (isFavorite) {
            favoriteButton.text = "Remove from Favorites"
            favoriteButton.setIconResource(android.R.drawable.btn_star_big_on)
        } else {
            favoriteButton.text = "Add to Favorites"
            favoriteButton.setIconResource(android.R.drawable.btn_star_big_off)
        }
    }
}