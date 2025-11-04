package com.ramankumar.moviefinder.ui.swipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramankumar.moviefinder.data.repository.MovieRepository
import com.ramankumar.moviefinder.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SwipeViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadMovies()
    }

    fun loadMovies() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getPopularMovies(forceRefresh = true)
                .onSuccess { movieList ->
                    _movies.value = movieList
                    _currentIndex.value = 0
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load movies"
                }

            _isLoading.value = false
        }
    }

    fun onSwipeRight() {
        val currentMovie = getCurrentMovie() ?: return

        viewModelScope.launch {
            repository.recordSwipe(currentMovie, liked = true)
            moveToNextMovie()
        }
    }

    fun onSwipeLeft() {
        val currentMovie = getCurrentMovie() ?: return

        viewModelScope.launch {
            repository.recordSwipe(currentMovie, liked = false)
            moveToNextMovie()
        }
    }

    private fun moveToNextMovie() {
        if (_currentIndex.value < _movies.value.size - 1) {
            _currentIndex.value++
        } else {
            loadMovies()
        }
    }

    fun getCurrentMovie(): Movie? {
        val index = _currentIndex.value
        val movieList = _movies.value
        return if (index < movieList.size) movieList[index] else null
    }
}