package com.ramankumar.moviefinder.ui.trailer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramankumar.moviefinder.data.repository.MovieRepository
import com.ramankumar.moviefinder.model.Video
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrailerViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    private val _trailers = MutableStateFlow<List<Video>>(emptyList())
    val trailers: StateFlow<List<Video>> = _trailers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Load trailers for a specific movie
     * Uses TMDb API only - no YouTube API key needed!
     */
    fun loadTrailers(movieId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            android.util.Log.d("TrailerViewModel", "loadTrailers: Loading for movie $movieId")

            repository.getMovieTrailers(movieId)
                .onSuccess { videoList ->
                    _trailers.value = videoList
                    android.util.Log.d("TrailerViewModel", "loadTrailers: SUCCESS - ${videoList.size} trailers")
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load trailers"
                    android.util.Log.e("TrailerViewModel", "loadTrailers: FAILURE - ${exception.message}")
                }

            _isLoading.value = false
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }
}