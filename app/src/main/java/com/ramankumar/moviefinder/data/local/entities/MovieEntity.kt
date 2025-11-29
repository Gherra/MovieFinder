package com.ramankumar.moviefinder.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ramankumar.moviefinder.model.Movie

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String,
    val releaseDate: String,
    val voteAverage: Double,
    val genreIds: List<Int>?,  // ← ADDED with TypeConverter support
    val popularity: Double,
    val originalLanguage: String,
    val isCached: Boolean = true,
    val cachedTimestamp: Long = System.currentTimeMillis()
)

fun MovieEntity.toMovie(): Movie {
    return Movie(
        id = this.id,
        title = this.title,
        posterPath = this.posterPath,
        overview = this.overview,
        releaseDate = this.releaseDate,
        voteAverage = this.voteAverage,
        backdropPath = this.backdropPath,
        genreIds = this.genreIds,
        // Vote count and popularity are only used for relevance will not be grabbed from
        // Cache/DB
        voteCount = 0,
        popularity = 0.0,
        isFavorite = false
    )
}

fun Movie.toEntity(): MovieEntity {
    return MovieEntity(
        id = this.id,
        title = this.title,
        posterPath = this.posterPath,
        backdropPath = this.backdropPath,
        overview = this.overview,
        releaseDate = this.releaseDate,
        voteAverage = this.voteAverage,
        genreIds = this.genreIds,
        popularity = 0.0,
        originalLanguage = ""
    )
}