package com.ramankumar.moviefinder

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.ramankumar.moviefinder.adapter.MovieAdapter
import com.ramankumar.moviefinder.api.ApiConfig
import com.ramankumar.moviefinder.api.RetrofitClient
import com.ramankumar.moviefinder.model.Movie
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var movieAdapter: MovieAdapter

    private var currentMovies = mutableListOf<Movie>()
    private var favoriteMovies = mutableListOf<Movie>()

    // Activity result launcher for returning from detail page
    private val detailActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val movieId = data?.getIntExtra("MOVIE_ID", 0) ?: 0
            val isFavorite = data?.getBooleanExtra("IS_FAVORITE", false) ?: false

            // Update the movie in current list
            currentMovies.find { it.id == movieId }?.isFavorite = isFavorite

            // Update favorites list
            if (isFavorite) {
                val movie = currentMovies.find { it.id == movieId }
                if (movie != null && favoriteMovies.none { it.id == movieId }) {
                    favoriteMovies.add(movie)
                }
            } else {
                favoriteMovies.removeAll { it.id == movieId }
            }

            movieAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        tabLayout = findViewById(R.id.tabLayout)
        recyclerView = findViewById(R.id.recyclerView)

        // Setup RecyclerView
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        movieAdapter = MovieAdapter(
            movies = currentMovies,
            onMovieClick = { movie -> openMovieDetail(movie) },
            onFavoriteClick = { movie -> toggleFavorite(movie) }
        )
        recyclerView.adapter = movieAdapter

        // Setup TabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Popular"))
        tabLayout.addTab(tabLayout.newTab().setText("Search"))
        tabLayout.addTab(tabLayout.newTab().setText("Favorites"))
        tabLayout.addTab(tabLayout.newTab().setText("Swipe"))

        // Load popular movies by default
        loadPopularMovies()

        // Tab selection listener
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadPopularMovies()
                    1 -> searchEditText.requestFocus()
                    2 -> showFavorites()
                    3 -> openSwipeActivity()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Search button click
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            if (query.isNotEmpty()) {
                searchMovies(query)
                tabLayout.selectTab(tabLayout.getTabAt(1))
            } else {
                Toast.makeText(this, "Please enter a movie name", Toast.LENGTH_SHORT).show()
            }
        }

        // Search on keyboard enter
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = searchEditText.text.toString()
                if (query.isNotEmpty()) {
                    searchMovies(query)
                    tabLayout.selectTab(tabLayout.getTabAt(1))
                }
                true
            } else {
                false
            }
        }
    }

    private fun loadPopularMovies() {
        lifecycleScope.launch {
            try {
                println("========================================")
                println("🔍 MAIN: Starting to load popular movies...")
                println("🔍 MAIN: API Key exists: ${ApiConfig.API_KEY.isNotEmpty()}")
                println("========================================")

                val response = RetrofitClient.api.getPopularMovies(ApiConfig.API_KEY)

                println("========================================")
                println("🔍 MAIN: SUCCESS! Got ${response.results.size} movies")
                println("========================================")

                currentMovies.clear()
                currentMovies.addAll(response.results)
                updateFavoriteStatus()
                movieAdapter.updateMovies(currentMovies)

                Toast.makeText(this@MainActivity, "Loaded ${response.results.size} movies!", Toast.LENGTH_LONG).show()

            } catch (e: Exception) {
                println("========================================")
                println("❌ MAIN: ERROR loading movies!")
                println("❌ MAIN: ${e.message}")
                e.printStackTrace()
                println("========================================")

                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun searchMovies(query: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.searchMovies(ApiConfig.API_KEY, query)
                currentMovies.clear()
                currentMovies.addAll(response.results)
                updateFavoriteStatus()
                movieAdapter.updateMovies(currentMovies)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showFavorites() {
        currentMovies.clear()
        currentMovies.addAll(favoriteMovies)
        movieAdapter.updateMovies(currentMovies)
    }

    private fun toggleFavorite(movie: Movie) {
        if (movie.isFavorite) {
            favoriteMovies.removeAll { it.id == movie.id }
            movie.isFavorite = false
        } else {
            favoriteMovies.add(movie.copy(isFavorite = true))
            movie.isFavorite = true
        }
        updateFavoriteStatus()
        movieAdapter.notifyDataSetChanged()
        Toast.makeText(
            this,
            if (movie.isFavorite) "Added to favorites" else "Removed from favorites",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateFavoriteStatus() {
        currentMovies.forEach { movie ->
            movie.isFavorite = favoriteMovies.any { it.id == movie.id }
        }
    }

    private fun openMovieDetail(movie: Movie) {
        val intent = Intent(this, MovieDetailActivity::class.java).apply {
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
        val intent = Intent(this, SwipeActivity::class.java)
        startActivity(intent)
    }
}