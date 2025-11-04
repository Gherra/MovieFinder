package com.ramankumar.moviefinder.ui.swipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ramankumar.moviefinder.data.repository.MovieRepository

class SwipeViewModelFactory(
    private val repository: MovieRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SwipeViewModel::class.java)) {
            return SwipeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}