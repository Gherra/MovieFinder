package com.ramankumar.moviefinder.ui.swipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramankumar.moviefinder.data.repository.MovieRepository
import com.ramankumar.moviefinder.data.repository.SwipeStats
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

    // Swipe statistics
    private val _swipeStats = MutableStateFlow<SwipeStats?>(null)
    val swipeStats: StateFlow<SwipeStats?> = _swipeStats.asStateFlow()

    // Show reset confirmation dialog
    private val _showResetDialog = MutableStateFlow(false)
    val showResetDialog: StateFlow<Boolean> = _showResetDialog.asStateFlow()

    private val _showInfoDialog = MutableStateFlow(false)
    val showInfoDialog: StateFlow<Boolean> = _showInfoDialog.asStateFlow()

    // Trailer cache: movieId -> youtubeKey
    private val _trailerCache = MutableStateFlow<Map<Int, String?>>(emptyMap())
    val trailerCache: StateFlow<Map<Int, String?>> = _trailerCache.asStateFlow()

    init {
        loadMovies()
        loadSwipeStats()
    }

    /**
     * Preload trailers for current and next 2 movies
     * This happens in the background to ensure smooth swipes
     */
    fun preloadTrailers(currentIndex: Int) {
        val movieList = _movies.value

        // Preload for current + next 2 movies
        for (offset in 0..2) {
            val index = currentIndex + offset
            if (index < movieList.size) {
                val movie = movieList[index]

                // Only fetch if not already cached
                if (!_trailerCache.value.containsKey(movie.id)) {
                    viewModelScope.launch {
                        repository.getMovieTrailers(movie.id)
                            .onSuccess { trailers ->
                                // Get best trailer (official, YouTube, highest priority)
                                val youtubeKey = trailers
                                    .filter { it.isYouTube() }
                                    .sortedByDescending { it.getPriority() }
                                    .firstOrNull()?.key

                                // Update cache
                                _trailerCache.value = _trailerCache.value + (movie.id to youtubeKey)

                                android.util.Log.d(
                                    "SwipeViewModel",
                                    "Preloaded trailer for ${movie.title}: ${youtubeKey ?: "none"}"
                                )
                            }
                            .onFailure { exception ->
                                android.util.Log.w(
                                    "SwipeViewModel",
                                    "Failed to preload trailer for ${movie.title}: ${exception.message}"
                                )
                                // Cache the failure so we don't retry
                                _trailerCache.value = _trailerCache.value + (movie.id to null)
                            }
                    }
                }
            }
        }

        // Clean up old cached trailers to save memory (keep only -1 to +3 range)
        val relevantMovieIds = (currentIndex - 1..currentIndex + 3)
            .mapNotNull { movieList.getOrNull(it)?.id }
            .toSet()

        _trailerCache.value = _trailerCache.value.filterKeys { it in relevantMovieIds }
    }

    /**
     * Get trailer key for a specific movie (from cache)
     */
    fun getTrailerKey(movieId: Int): String? {
        return _trailerCache.value[movieId]
    }

    fun loadMovies() {
        android.util.Log.d("SwipeViewModel", "loadMovies: Called!")

        viewModelScope.launch {
            _isLoading.value = true
            android.util.Log.d("SwipeViewModel", "loadMovies: isLoading set to true")
            _error.value = null

            repository.getShuffledMovies()
                .onSuccess { movieList ->
                    android.util.Log.d("SwipeViewModel", "loadMovies: SUCCESS - Got ${movieList.size} movies")
                    _movies.value = movieList
                    _currentIndex.value = 0
                    // Preload trailers for first few movies
                    preloadTrailers(0)
                }
                .onFailure { exception ->
                    android.util.Log.e("SwipeViewModel", "loadMovies: FAILURE - ${exception.message}")
                    _error.value = exception.message ?: "Failed to load movies"
                }

            _isLoading.value = false
            android.util.Log.d("SwipeViewModel", "loadMovies: isLoading set to false")
        }
    }

    fun onSwipeRight() {
        val currentMovie = getCurrentMovie() ?: return

        viewModelScope.launch {
            repository.recordSwipe(currentMovie, liked = true, neutral = false)
            moveToNextMovie()
            loadSwipeStats()
            // Preload trailers for upcoming movies
            preloadTrailers(_currentIndex.value)
        }
    }

    fun onSwipeLeft() {
        val currentMovie = getCurrentMovie() ?: return

        viewModelScope.launch {
            repository.recordSwipe(currentMovie, liked = false, neutral = false)
            moveToNextMovie()
            loadSwipeStats()
            // Preload trailers for upcoming movies
            preloadTrailers(_currentIndex.value)
        }
    }

    fun onSwipeUp() {
        val currentMovie = getCurrentMovie() ?: return

        viewModelScope.launch {
            repository.recordSwipe(currentMovie, liked = false, neutral = true)
            moveToNextMovie()
            loadSwipeStats()
            // Preload trailers for upcoming movies
            preloadTrailers(_currentIndex.value)
            android.util.Log.d("SwipeViewModel", "onSwipeUp: Recorded neutral swipe for ${currentMovie.title}")
        }
    }

    fun loadSwipeStats() {
        viewModelScope.launch {
            val stats = repository.getSwipeStats()
            _swipeStats.value = stats
            android.util.Log.d("SwipeViewModel", "loadSwipeStats: ${stats.totalSwipes} total, ${stats.liked} liked, ${stats.disliked} disliked, ${stats.neutral} neutral")
        }
    }

    fun showResetDialog() {
        _showResetDialog.value = true
    }

    fun hideResetDialog() {
        _showResetDialog.value = false
    }

    fun showInfoDialog() {
        _showInfoDialog.value = true
    }

    fun hideInfoDialog() {
        _showInfoDialog.value = false
    }

    fun resetSwipeHistory() {
        viewModelScope.launch {
            repository.clearSwipeHistory()
            _swipeStats.value = SwipeStats(0, 0, 0, 0)
            hideResetDialog()
            loadMovies() // Reload movies to show all again
            android.util.Log.d("SwipeViewModel", "resetSwipeHistory: History cleared")
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