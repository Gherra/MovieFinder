package com.ramankumar.moviefinder.model

data class MovieRelevance (
    val movie: Movie,
    val pageIndex: Int,
    val positionInPage: Int
)
