package com.ramankumar.moviefinder.model

import com.google.gson.annotations.SerializedName

data class Movie(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("poster_path")
    val posterPath: String?,

    @SerializedName("overview")
    val overview: String,

    @SerializedName("release_date")
    val releaseDate: String,

    @SerializedName("vote_average")
    val voteAverage: Double,

    @SerializedName("backdrop_path")
    val backdropPath: String?,

    var isFavorite: Boolean = false
) {
    fun getPosterUrl(): String {
        return "https://image.tmdb.org/t/p/w500$posterPath"
    }

    fun getBackdropUrl(): String {
        return "https://image.tmdb.org/t/p/w780$backdropPath"
    }
}

data class MovieResponse(
    @SerializedName("results")
    val results: List<Movie>
)