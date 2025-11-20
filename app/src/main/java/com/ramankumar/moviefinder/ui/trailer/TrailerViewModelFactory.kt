package com.ramankumar.moviefinder.ui.trailer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ramankumar.moviefinder.data.repository.MovieRepository

class TrailerViewModelFactory(
    private val repository: MovieRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrailerViewModel::class.java)) {
            return TrailerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}