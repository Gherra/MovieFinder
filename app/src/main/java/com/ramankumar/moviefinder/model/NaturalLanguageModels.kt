package com.ramankumar.moviefinder.model

import com.google.gson.annotations.SerializedName

data class GeminiKeywordResponse(
    val keywords: List<String> = emptyList(),
    val genres: List<String> = emptyList(),
    val vibes: List<String> = emptyList()
)

data class TMDbKeyword(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
)

data class TMDbKeywordSearchResponse(
    @SerializedName("results")
    val results: List<TMDbKeyword>
)

data class NaturalLanguageSearchResult(
    val movies: List<Movie>,
    val geminiResponse: GeminiKeywordResponse,
    val matchedKeywords: List<TMDbKeyword>,
    val usedGenreIds: List<Int>
)
