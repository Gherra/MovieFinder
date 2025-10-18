package com.ramankumar.moviefinder

import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.ramankumar.moviefinder.api.ApiConfig
import com.ramankumar.moviefinder.api.RetrofitClient
import com.ramankumar.moviefinder.model.Movie
import kotlinx.coroutines.launch
import kotlin.math.abs

class SwipeActivity : AppCompatActivity() {

    private lateinit var cardView: MaterialCardView
    private lateinit var posterImageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var yearTextView: TextView
    private lateinit var ratingTextView: TextView
    private lateinit var overviewTextView: TextView

    private var movies = mutableListOf<Movie>()
    private var currentIndex = 0
    private val likedMovies = mutableListOf<Movie>()
    private val dislikedMovies = mutableListOf<Movie>()

    private var downX = 0f
    private var downY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swipe)

        // Initialize views
        cardView = findViewById(R.id.swipeCardView)
        posterImageView = findViewById(R.id.swipePosterImageView)
        titleTextView = findViewById(R.id.swipeTitleTextView)
        yearTextView = findViewById(R.id.swipeYearTextView)
        ratingTextView = findViewById(R.id.swipeRatingTextView)
        overviewTextView = findViewById(R.id.swipeOverviewTextView)

        // Simple touch listener
        cardView.setOnTouchListener { view, event ->
            println("👆 TOUCH EVENT: ${event.action}")

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    println("👆 DOWN at X=${event.rawX}, Y=${event.rawY}")
                    downX = event.rawX
                    downY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - downX
                    val deltaY = event.rawY - downY

                    println("👆 MOVE deltaX=$deltaX")

                    view.translationX = deltaX
                    view.translationY = deltaY
                    view.rotation = deltaX / 20f
                    view.alpha = 1f - (abs(deltaX) / 1000f)

                    true
                }
                MotionEvent.ACTION_UP -> {
                    val deltaX = event.rawX - downX

                    println("👆 UP deltaX=$deltaX")

                    if (abs(deltaX) > 200) {
                        if (deltaX > 0) {
                            println("👆 SWIPED RIGHT!")
                            animateSwipeRight()
                        } else {
                            println("👆 SWIPED LEFT!")
                            animateSwipeLeft()
                        }
                    } else {
                        println("👆 Reset - not far enough")
                        view.animate()
                            .translationX(0f)
                            .translationY(0f)
                            .rotation(0f)
                            .alpha(1f)
                            .setDuration(200)
                            .start()
                    }
                    true
                }
                else -> false
            }
        }

        // Load popular movies
        loadMovies()
    }

    private fun loadMovies() {
        lifecycleScope.launch {
            try {
                println("🔍 DEBUG: Starting to load movies...")
                val response = RetrofitClient.api.getPopularMovies(ApiConfig.API_KEY)
                println("🔍 DEBUG: API Response received! Movies count: ${response.results.size}")

                movies.addAll(response.results)
                if (movies.isNotEmpty()) {
                    println("🔍 DEBUG: Displaying first movie: ${movies[0].title}")
                    displayCurrentMovie()
                }
            } catch (e: Exception) {
                println("❌ DEBUG: Error loading movies: ${e.message}")
                e.printStackTrace()
                Toast.makeText(this@SwipeActivity, "Error loading movies: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayCurrentMovie() {
        if (currentIndex >= movies.size) {
            Toast.makeText(this, "No more movies! Restarting...", Toast.LENGTH_SHORT).show()
            currentIndex = 0
        }

        val movie = movies[currentIndex]

        titleTextView.text = movie.title
        yearTextView.text = movie.releaseDate.take(4)
        ratingTextView.text = getString(R.string.rating_format, movie.voteAverage)
        overviewTextView.text = movie.overview

        Glide.with(this)
            .load(movie.getPosterUrl())
            .placeholder(R.drawable.ic_launcher_background)
            .into(posterImageView)

        // Reset card
        cardView.translationX = 0f
        cardView.translationY = 0f
        cardView.rotation = 0f
        cardView.alpha = 1f
    }

    private fun animateSwipeRight() {
        val movie = movies[currentIndex]
        likedMovies.add(movie)

        cardView.animate()
            .translationX(2000f)
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                Toast.makeText(this, "❤️ Liked: ${movie.title}", Toast.LENGTH_SHORT).show()
                currentIndex++
                displayCurrentMovie()
            }
            .start()
    }

    private fun animateSwipeLeft() {
        val movie = movies[currentIndex]
        dislikedMovies.add(movie)

        cardView.animate()
            .translationX(-2000f)
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                Toast.makeText(this, "👎 Passed: ${movie.title}", Toast.LENGTH_SHORT).show()
                currentIndex++
                displayCurrentMovie()
            }
            .start()
    }
}