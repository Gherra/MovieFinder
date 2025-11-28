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

    // SEPARATE STATES FOR EACH FILTER
    private val _popularMovies = MutableStateFlow<List<Movie>>(emptyList())
    private val _trendingMovies = MutableStateFlow<List<Movie>>(emptyList())
    private val _topRatedMovies = MutableStateFlow<List<Movie>>(emptyList())
    private val _nowPlayingMovies = MutableStateFlow<List<Movie>>(emptyList())
    private val _searchResults = MutableStateFlow<List<Movie>>(emptyList())
    private val _favorites = MutableStateFlow<List<Movie>>(emptyList())
    private val _recommendations = MutableStateFlow<List<Movie>>(emptyList())


    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies.asStateFlow()

    val favorites: StateFlow<List<Movie>> = _favorites.asStateFlow()
    val recommendations: StateFlow<List<Movie>> = _recommendations.asStateFlow()


    private val _currentFilter = MutableStateFlow(0)

    private val _isFirstSearch = MutableStateFlow(true)
    val isFirstSearch: StateFlow<Boolean> = _isFirstSearch.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()


    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    init {
        loadTrendingMovies()
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
            _currentFilter.value = 0

            android.util.Log.d("MainViewModel", "loadPopularMovies: Starting (refresh=$refresh)")

            repository.getPopularMovies(
                startPage = 1,
                pageCount = 5,
                forceRefresh = refresh
            )
                .onSuccess { movieList ->
                    android.util.Log.d("MainViewModel", "loadPopularMovies: SUCCESS - ${movieList.size} movies")
                    _popularMovies.value = movieList
                    // Only update displayed movies if user is still on Trending filter
                    if (_currentFilter.value == 0) {
                        _movies.value = movieList
                    }
                }
                .onFailure { exception ->
                    android.util.Log.e("MainViewModel", "loadPopularMovies: FAILURE - ${exception.message}")
                    _error.value = exception.message ?: "Failed to load movies"
                }

            _isLoading.value = false
            _isRefreshing.value = false
        }
    }

    fun loadTrendingMovies(refresh: Boolean = false) {
        viewModelScope.launch {
            if (refresh) _isRefreshing.value = true else _isLoading.value = true
            _error.value = null
            _currentFilter.value = 0

            repository.getTrendingMovies(forceRefresh = refresh)
                .onSuccess {
                    _trendingMovies.value = it
                    if (_currentFilter.value == 0) _movies.value = it
                }
                .onFailure { _error.value = it.message }

            _isLoading.value = false
            _isRefreshing.value = false
        }
    }
    fun loadTopRatedMovies(refresh: Boolean = false) {
        viewModelScope.launch {
            if (refresh) {
                _isRefreshing.value = true
            } else {
                _isLoading.value = true
            }
            _error.value = null
            _currentFilter.value = 1

            android.util.Log.d("MainViewModel", "loadTopRatedMovies: Starting (refresh=$refresh)")

            repository.getTopRatedMovies(startPage = 1, pageCount = 5)
                .onSuccess { movieList ->
                    android.util.Log.d("MainViewModel", "loadTopRatedMovies: SUCCESS - ${movieList.size} movies")
                    _topRatedMovies.value = movieList
                    // Only update displayed movies if user is still on Top Rated filter
                    if (_currentFilter.value == 1) {
                        _movies.value = movieList
                    }
                }
                .onFailure { exception ->
                    android.util.Log.e("MainViewModel", "loadTopRatedMovies: FAILURE - ${exception.message}")
                    _error.value = exception.message ?: "Failed to load top rated movies"
                }

            _isLoading.value = false
            _isRefreshing.value = false
        }
    }

    fun loadNowPlayingMovies(refresh: Boolean = false) {
        viewModelScope.launch {
            if (refresh) {
                _isRefreshing.value = true
            } else {
                _isLoading.value = true
            }
            _error.value = null
            _currentFilter.value = 2  // 2 = Recent / Now Playing

            android.util.Log.d("MainViewModel", "loadNowPlayingMovies: Starting (refresh=$refresh)")

            repository.getNowPlayingMovies(startPage = 1, pageCount = 5)
                .onSuccess { movieList ->
                    android.util.Log.d("MainViewModel", "loadNowPlayingMovies: SUCCESS - ${movieList.size} movies")
                    _nowPlayingMovies.value = movieList

                    // Only show now playing if Explore + Recent are selected
                    if (_currentTab.value == 0 && _currentFilter.value == 2) {
                        _movies.value = movieList
                    }
                }
                .onFailure { exception ->
                    android.util.Log.e("MainViewModel", "loadNowPlayingMovies: FAILURE - ${exception.message}")
                    _error.value = exception.message ?: "Failed to load recent movies"
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
            _isFirstSearch.value = false
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

            // Update the movie in ALL lists
            _popularMovies.value = _popularMovies.value.map { movie ->
                if (movie.id == movieId) movie.copy(isFavorite = newStatus) else movie
            }

            _topRatedMovies.value = _topRatedMovies.value.map { movie ->
                if (movie.id == movieId) movie.copy(isFavorite = newStatus) else movie
            }

            _nowPlayingMovies.value = _nowPlayingMovies.value.map { movie ->
                if (movie.id == movieId) movie.copy(isFavorite = newStatus) else movie
            }

            _searchResults.value = _searchResults.value.map { movie ->
                if (movie.id == movieId) movie.copy(isFavorite = newStatus) else movie
            }

            _recommendations.value = _recommendations.value.map { movie ->
                if (movie.id == movieId) movie.copy(isFavorite = newStatus) else movie
            }

            // Update the displayed list
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

                // EXPLORE TAB
                // based on current filter load in the movies required
                loadExploreTabFilter(_currentFilter.value)

                _movies.value = when (_currentFilter.value) {
                    0 -> _trendingMovies.value
                    1 -> _topRatedMovies.value
                    2 -> _nowPlayingMovies.value
                    else -> _trendingMovies.value
                }


            }
            1 -> {

                _movies.value = _searchResults.value
            }
            2 -> {

                _movies.value = _favorites.value
            }
        }
    }

    /**
     * Helper function to enable lazy loading. Checks if the current filter
     * within explore tab has already been loaded/cached. If not then load. This prevents
     * excessive loading for filters that user will not use.
     */
    fun loadExploreTabFilter(currentFilter: Int){
        when(currentFilter){
            0-> if(_trendingMovies.value.isEmpty()) loadTrendingMovies()
            1-> if(_topRatedMovies.value.isEmpty()) loadTopRatedMovies()
            2-> if(_nowPlayingMovies.value.isEmpty()) loadNowPlayingMovies()
        }
    }

    fun getCurrentMovies(): List<Movie> {
        return when (_currentTab.value) {
            0 -> when (_currentFilter.value) {
                0 -> _trendingMovies.value
                1 -> _topRatedMovies.value
                2 -> _nowPlayingMovies.value
                else -> _trendingMovies.value
            }
            1 -> _searchResults.value
            2 -> _favorites.value
            else -> _movies.value
        }
    }

    fun loadRecommendations() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            android.util.Log.d("MainViewModel", "loadRecommendations: Starting...")

            repository.getRecommendedMovies()
                .onSuccess { movieList ->
                    _recommendations.value = movieList
                    _movies.value = movieList
                    android.util.Log.d("MainViewModel", "loadRecommendations: SUCCESS - ${movieList.size} recommendations")
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load recommendations"
                    android.util.Log.e("MainViewModel", "loadRecommendations: FAILURE - ${exception.message}")
                }

            _isLoading.value = false
        }
    }
}