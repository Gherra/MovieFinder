package com.ramankumar.moviefinder.api

import com.ramankumar.moviefinder.model.MovieResponse
import com.ramankumar.moviefinder.model.VideoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TMDbApi {

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): Response<MovieResponse>

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): Response<MovieResponse>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): Response<MovieResponse>

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): Response<MovieResponse>

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("api_key") apiKey: String,
        @Query("with_genres") withGenres: String? = null,
        @Query("vote_average.gte") minRating: Double? = null,
        @Query("vote_average.lte") maxRating: Double? = null,
        @Query("primary_release_date.gte") releaseDateFrom: String? = null,
        @Query("primary_release_date.lte") releaseDateTo: String? = null,
        @Query("vote_count.gte") voteCountGte: Int? = null,
        @Query("with_original_language") originalLanguage: String? = null,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("page") page: Int = 1
    ): Response<MovieResponse>

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): Response<VideoResponse>
}