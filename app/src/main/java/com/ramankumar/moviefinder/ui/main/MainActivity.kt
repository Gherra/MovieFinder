package com.ramankumar.moviefinder.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.ramankumar.moviefinder.R
import com.ramankumar.moviefinder.adapter.MovieAdapter
import com.ramankumar.moviefinder.api.ApiConfig
import com.ramankumar.moviefinder.api.RetrofitClient
import com.ramankumar.moviefinder.data.local.AppDatabase
import com.ramankumar.moviefinder.data.repository.MovieRepository
import com.ramankumar.moviefinder.ui.detail.MovieDetailActivity
import com.ramankumar.moviefinder.ui.swipe.SwipeActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var movieAdapter: MovieAdapter

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
            viewModel.loadPopularMovies()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupRecyclerView()
        setupTabLayout()
        setupSearchButton()
        observeViewModel()
    }

    private fun initializeViews() {
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        tabLayout = findViewById(R.id.tabLayout)
        recyclerView = findViewById(R.id.recyclerView)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        movieAdapter = MovieAdapter(
            movies = emptyList(),
            onMovieClick = { movie -> openMovieDetail(movie) },
            onFavoriteClick = { movie -> viewModel.toggleFavorite(movie.id) }
        )
        recyclerView.adapter = movieAdapter
    }

    private fun setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Popular"))
        tabLayout.addTab(tabLayout.newTab().setText("Search"))
        tabLayout.addTab(tabLayout.newTab().setText("Favorites"))
        tabLayout.addTab(tabLayout.newTab().setText("Swipe"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.setCurrentTab(0)
                    1 -> {
                        viewModel.setCurrentTab(1)  //tellign viewmodel were on the search tab!
                        searchEditText.requestFocus()
                    }
                    2 -> viewModel.setCurrentTab(2)
                    3 -> openSwipeActivity()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSearchButton() {
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            if (query.isNotEmpty()) {
                viewModel.searchMovies(query)
                tabLayout.selectTab(tabLayout.getTabAt(1))
            } else {
                Toast.makeText(this, "Please enter a movie name", Toast.LENGTH_SHORT).show()
            }
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = searchEditText.text.toString()
                if (query.isNotEmpty()) {
                    viewModel.searchMovies(query)
                    tabLayout.selectTab(tabLayout.getTabAt(1))
                }
                true
            } else {
                false
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.movies.collect { movies ->
                movieAdapter.updateMovies(viewModel.getCurrentMovies())
            }
        }

        lifecycleScope.launch {
            viewModel.favorites.collect { favorites ->
                if (viewModel.currentTab.value == 2) {
                    movieAdapter.updateMovies(favorites)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                // TODO: Show/hide loading indicator if you add one later
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.currentTab.collect { tab ->
                movieAdapter.updateMovies(viewModel.getCurrentMovies())
            }
        }
    }

    private fun openMovieDetail(movie: com.ramankumar.moviefinder.model.Movie) {
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