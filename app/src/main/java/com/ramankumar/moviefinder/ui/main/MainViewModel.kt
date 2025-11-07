package com.ramankumar.moviefinder.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramankumar.moviefinder.data.repository.MovieRepository
import com.ramankumar.moviefinder.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    // Separate StateFlows for each tab!
    private val _popularMovies = MutableStateFlow<List<Movie>>(emptyList())
    private val _searchResults = MutableStateFlow<List<Movie>>(emptyList())
    private val _favorites = MutableStateFlow<List<Movie>>(emptyList())

    // This is what the UI observes - changes based on current tab
    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies.asStateFlow()

    val favorites: StateFlow<List<Movie>> = _favorites.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Pagination state
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    init {
        loadPopularMovies()
        observeFavorites()
    }

    fun loadPopularMovies(refresh: Boolean = false) {
        viewModelScope.launch {
            if (refresh) {
                _isRefreshing.value = true
                _currentPage.value = 1
            } else {
                _isLoading.value = true
            }
            _error.value = null

            repository.getPopularMovies(
                startPage = 1,
                pageCount = 5,  // Load 5 pages (~100 movies)
                forceRefresh = refresh
            )
                .onSuccess { movieList ->
                    _popularMovies.value = movieList
                    // If we're currently on Popular tab, update display
                    if (_currentTab.value == 0) {
                        _movies.value = movieList
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load movies"
                }

            _isLoading.value = false
            _isRefreshing.value = false
        }
    }

    fun searchMovies(query: String) {
        if (query.isEmpty()) {
            // Clear search results if query is empty
            _searchResults.value = emptyList()
            if (_currentTab.value == 1) {
                _movies.value = emptyList()
            }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.searchMovies(query)
                .onSuccess { movieList ->
                    _searchResults.value = movieList
                    // If we're on Search tab, update display
                    if (_currentTab.value == 1) {
                        _movies.value = movieList
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Search failed"
                }

            _isLoading.value = false
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.getAllFavorites().collect { favoriteList ->
                _favorites.value = favoriteList
                // If we're on Favorites tab, update display
                if (_currentTab.value == 2) {
                    _movies.value = favoriteList
                }
            }
        }
    }

    fun toggleFavorite(movieId: Int) {
        viewModelScope.launch {
            val newStatus = repository.toggleFavorite(movieId)

            // Update the movie in whichever list it's in
            _popularMovies.value = _popularMovies.value.map { movie ->
                if (movie.id == movieId) movie.copy(isFavorite = newStatus) else movie
            }

            _searchResults.value = _searchResults.value.map { movie ->
                if (movie.id == movieId) movie.copy(isFavorite = newStatus) else movie
            }

            // Update the displayed list if needed
            _movies.value = _movies.value.map { movie ->
                if (movie.id == movieId) movie.copy(isFavorite = newStatus) else movie
            }
        }
    }

    fun setCurrentTab(position: Int) {
        _currentTab.value = position

        // Update _movies based on which tab is selected
        when (position) {
            0 -> {
                // Popular tab - show popular movies
                _movies.value = _popularMovies.value
                // Load popular movies if empty
                if (_popularMovies.value.isEmpty()) {
                    loadPopularMovies()
                }
            }
            1 -> {
                // Search tab - show last search results (keeps your search!)
                _movies.value = _searchResults.value
            }
            2 -> {
                // Favorites tab - show favorites
                _movies.value = _favorites.value
            }
            // Tab 3 (Swipe) opens new Activity - no data change needed
        }
    }

    fun getCurrentMovies(): List<Movie> {
        return when (_currentTab.value) {
            0 -> _popularMovies.value
            1 -> _searchResults.value
            2 -> _favorites.value
            else -> _movies.value
        }
    }

    fun loadTopRatedMovies() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getTopRatedMovies(startPage = 1, pageCount = 5)
                .onSuccess { movieList ->
                    _movies.value = movieList
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load top rated movies"
                }

            _isLoading.value = false
        }
    }

    fun loadNowPlayingMovies() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getNowPlayingMovies(startPage = 1, pageCount = 5)
                .onSuccess { movieList ->
                    _movies.value = movieList
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load recent movies"
                }

            _isLoading.value = false
        }
    }


}