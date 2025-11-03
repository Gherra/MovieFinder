package com.ramankumar.moviefinder.ui.swipe

import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.ramankumar.moviefinder.R
import com.ramankumar.moviefinder.api.ApiConfig
import com.ramankumar.moviefinder.api.RetrofitClient
import com.ramankumar.moviefinder.data.local.AppDatabase
import com.ramankumar.moviefinder.data.repository.MovieRepository
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

    private var downX = 0f
    private var downY = 0f

    private val viewModel: SwipeViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = MovieRepository(
            movieDao = database.movieDao(),
            swipeHistoryDao = database.swipeHistoryDao(),
            favoriteDao = database.favoriteDao(),
            api = RetrofitClient.api,
            apiKey = ApiConfig.API_KEY
        )
        SwipeViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swipe)

        initializeViews()
        setupTouchListener()
        observeViewModel()
    }

    private fun initializeViews() {
        cardView = findViewById(R.id.swipeCardView)
        posterImageView = findViewById(R.id.swipePosterImageView)
        titleTextView = findViewById(R.id.swipeTitleTextView)
        yearTextView = findViewById(R.id.swipeYearTextView)
        ratingTextView = findViewById(R.id.swipeRatingTextView)
        overviewTextView = findViewById(R.id.swipeOverviewTextView)
    }

    private fun observeViewModel() {
        // Observe movies loading
        lifecycleScope.launch {
            viewModel.movies.collect { movies ->
                if (movies.isNotEmpty()) {
                    displayCurrentMovie()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.currentIndex.collect { index ->
                displayCurrentMovie()
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                if (isLoading) {
                    Toast.makeText(this@SwipeActivity, "Loading movies...", Toast.LENGTH_SHORT).show()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(this@SwipeActivity, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupTouchListener() {
        cardView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - downX
                    val deltaY = event.rawY - downY

                    view.translationX = deltaX
                    view.translationY = deltaY
                    view.rotation = deltaX / 20f
                    view.alpha = 1f - (abs(deltaX) / 1000f)

                    true
                }
                MotionEvent.ACTION_UP -> {
                    val deltaX = event.rawX - downX

                    if (abs(deltaX) > 200) {
                        if (deltaX > 0) {
                            animateSwipeRight()
                        } else {
                            animateSwipeLeft()
                        }
                    } else {
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
    }

    private fun displayCurrentMovie() {
        val movie = viewModel.getCurrentMovie()

        if (movie == null) {
            Toast.makeText(this, "No more movies!", Toast.LENGTH_SHORT).show()
            return
        }

        titleTextView.text = movie.title
        yearTextView.text = movie.releaseDate.take(4)
        ratingTextView.text = getString(R.string.rating_format, movie.voteAverage)
        overviewTextView.text = movie.overview

        Glide.with(this)
            .load(movie.getPosterUrl())
            .placeholder(R.drawable.ic_launcher_background)
            .into(posterImageView)

        cardView.translationX = 0f
        cardView.translationY = 0f
        cardView.rotation = 0f
        cardView.alpha = 1f
    }

    private fun animateSwipeRight() {
        val movie = viewModel.getCurrentMovie() ?: return

        cardView.animate()
            .translationX(2000f)
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                Toast.makeText(this, "❤️ Liked: ${movie.title}", Toast.LENGTH_SHORT).show()
                viewModel.onSwipeRight()
            }
            .start()
    }

    private fun animateSwipeLeft() {
        val movie = viewModel.getCurrentMovie() ?: return

        cardView.animate()
            .translationX(-2000f)
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                Toast.makeText(this, "👎 Passed: ${movie.title}", Toast.LENGTH_SHORT).show()
                viewModel.onSwipeLeft()
            }
            .start()
    }
}