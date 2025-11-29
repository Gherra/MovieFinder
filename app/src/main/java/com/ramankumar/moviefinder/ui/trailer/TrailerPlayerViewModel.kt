package com.ramankumar.moviefinder.ui.trailer

import androidx.lifecycle.ViewModel

/**
 * ViewModel for TrailerPlayerActivity
 */
class TrailerPlayerViewModel : ViewModel() {
    
    private var lastPosition: Float = 0f

    fun updatePosition(position: Float) {
        lastPosition = position
    }

    fun getLastPosition(): Float {
        return lastPosition
    }
}

