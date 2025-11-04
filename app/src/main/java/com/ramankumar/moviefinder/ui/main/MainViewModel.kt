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

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies.asStateFlow()

    private val _favorites = MutableStateFlow<List<Movie>>(emptyList())
    val favorites: StateFlow<List<Movie>> = _favorites.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    init {
        loadPopularMovies()
        observeFavorites()
    }

    fun loadPopularMovies() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getPopularMovies(forceRefresh = false)
                .onSuccess { movieList ->
                    _movies.value = movieList
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load movies"
                }

            _isLoading.value = false
        }
    }

    fun searchMovies(query: String) {
        if (query.isEmpty()) {
            loadPopularMovies()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.searchMovies(query)
                .onSuccess { movieList ->
                    _movies.value = movieList
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
            }
        }
    }

    fun toggleFavorite(movieId: Int) {
        viewModelScope.launch {
            val newStatus = repository.toggleFavorite(movieId)

            _movies.value = _movies.value.map { movie ->
                if (movie.id == movieId) {
                    movie.copy(isFavorite = newStatus)
                } else {
                    movie
                }
            }
        }
    }

    fun setCurrentTab(position: Int) {
        _currentTab.value = position
        when (position) {
            0 -> loadPopularMovies()
            2 -> {
                _movies.value = _favorites.value
            }
        }
    }

    fun getCurrentMovies(): List<Movie> {
        return when (_currentTab.value) {
            2 -> _favorites.value
            else -> _movies.value
        }
    }
}