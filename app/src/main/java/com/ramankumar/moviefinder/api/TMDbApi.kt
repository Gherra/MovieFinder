package com.ramankumar.moviefinder.api

import com.ramankumar.moviefinder.model.MovieResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TMDbApi {

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): MovieResponse

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): MovieResponse
}