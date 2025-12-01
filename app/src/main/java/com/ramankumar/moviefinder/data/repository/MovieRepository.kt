package com.ramankumar.moviefinder.data.repository

import com.ramankumar.moviefinder.api.GeminiService
import com.ramankumar.moviefinder.api.TMDbApi
import com.ramankumar.moviefinder.data.local.dao.FavoriteDao
import com.ramankumar.moviefinder.data.local.dao.MovieDao
import com.ramankumar.moviefinder.data.local.dao.SwipeHistoryDao
import com.ramankumar.moviefinder.data.local.entities.FavoriteEntity
import com.ramankumar.moviefinder.data.local.entities.SwipeHistoryEntity
import com.ramankumar.moviefinder.data.local.entities.toEntity
import com.ramankumar.moviefinder.data.local.entities.toMovie
import com.ramankumar.moviefinder.model.Movie
import com.ramankumar.moviefinder.model.MovieRelevance
import com.ramankumar.moviefinder.model.NaturalLanguageSearchResult
import com.ramankumar.moviefinder.model.TMDbKeyword
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import com.ramankumar.moviefinder.model.Video

class MovieRepository(
    private val movieDao: MovieDao,
    private val swipeHistoryDao: SwipeHistoryDao,
    private val favoriteDao: FavoriteDao,
    private val api: TMDbApi,
    private val apiKey: String,
    private val geminiService: GeminiService
) {

    // PAGINATION SUPPORT

    suspend fun getTrendingMovies(
        startPage: Int = 1,
        pageCount: Int = 5,
        forceRefresh: Boolean = false
    ): Result<List<Movie>> {
        return try {
            val collected = mutableListOf<Movie>()
            for (page in startPage until (startPage + pageCount)) {
                val resp = api.getTrendingMovies(apiKey, page)
                if (!resp.isSuccessful) {
                    return Result.failure(IllegalStateException("Trending failed code=${resp.code()}"))
                }
                val results = resp.body()?.results.orEmpty()
                collected += results
                // Optional cache
                results.forEach { movieDao.insertMovie(it.toEntity()) }
            }
            Result.success(collected.distinctBy { it.id })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTopRatedMovies(
        startPage: Int = 1,
        pageCount: Int = 5
    ): Result<List<Movie>> {
        return try {
            val allMovies = mutableListOf<Movie>()

            for (page in startPage until startPage + pageCount) {
                val response = api.getTopRatedMovies(apiKey, page = page)
                val movies = response.body()?.results ?: emptyList()
                allMovies.addAll(movies)
            }

            android.util.Log.d("MovieRepository", "getTopRatedMovies: Total = ${allMovies.size}")
            Result.success(allMovies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNowPlayingMovies(
        startPage: Int = 1,
        pageCount: Int = 5
    ): Result<List<Movie>> {
        return try {
            val allMovies = mutableListOf<Movie>()

            for (page in startPage until startPage + pageCount) {
                val response = api.getNowPlayingMovies(apiKey, page = page)
                val movies = response.body()?.results ?: emptyList()
                allMovies.addAll(movies)
            }

            android.util.Log.d("MovieRepository", "getNowPlayingMovies: Total = ${allMovies.size}")
            Result.success(allMovies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // SWIPE FUNCTIONALITY

    /**
     * Gets shuffled movies for swiping, excluding already swiped ones
     * Fetches 10 pages (~200 movies) for variety
     */
    suspend fun getShuffledMovies(): Result<List<Movie>> {
        return try {
            android.util.Log.d("MovieRepository", "getShuffledMovies: Starting API call")

            // Fetch multiple pages for variety (10 pages = ~200 movies)
            val allMovies = mutableListOf<Movie>()
            for (page in 1..10) {
                val response = api.getPopularMovies(apiKey, page = page)
                val movies = response.body()?.results ?: emptyList()
                allMovies.addAll(movies)
            }

            android.util.Log.d("MovieRepository", "getShuffledMovies: Fetched ${allMovies.size} movies from 10 pages")

            // Get list of already swiped movie IDs
            val swipedIds = swipeHistoryDao.getAllSwipedMovieIds()
            android.util.Log.d("MovieRepository", "getShuffledMovies: ${swipedIds.size} movies already swiped")

            // Filter out swiped movies
            val unswipedMovies = allMovies.filter { it.id !in swipedIds }
            android.util.Log.d("MovieRepository", "getShuffledMovies: ${unswipedMovies.size} unswiped movies available")

            if (unswipedMovies.isEmpty()) {
                android.util.Log.e("MovieRepository", "getShuffledMovies: No unswiped movies! Returning all movies.")
                // If all movies swiped, return all (user can reset history)
                val shuffled = allMovies.shuffled()
                return Result.success(shuffled)
            }

            val shuffledMovies = unswipedMovies.shuffled()
            android.util.Log.d("MovieRepository", "getShuffledMovies: Returning ${shuffledMovies.size} shuffled movies")

            Result.success(shuffledMovies)
        } catch (e: Exception) {
            android.util.Log.e("MovieRepository", "getShuffledMovies: Exception - ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Records a swipe action (left = dislike, right = like, up = neutral)
     * @param neutral Set to true for "not sure" swipes (swipe up gesture)
     */
    suspend fun recordSwipe(movie: Movie, liked: Boolean, neutral: Boolean = false) {
        val swipe = SwipeHistoryEntity(
            movieId = movie.id,
            movieTitle = movie.title,
            swipedRight = liked,
            swipedNeutral = neutral
        )
        swipeHistoryDao.insertSwipe(swipe)
        movieDao.insertMovie(movie.toEntity())

        android.util.Log.d("MovieRepository", "recordSwipe: Movie ${movie.id} - liked=$liked, neutral=$neutral")
    }

    /**
     * Clears all swipe history (for reset functionality)
     */
    suspend fun clearSwipeHistory() {
        swipeHistoryDao.deleteAllSwipes()
        android.util.Log.d("MovieRepository", "clearSwipeHistory: All swipe history cleared")
    }

    suspend fun getSwipeStats(): SwipeStats {
        return SwipeStats(
            totalSwipes = swipeHistoryDao.getTotalSwipeCount(),
            liked = swipeHistoryDao.getLikedCount(),
            disliked = swipeHistoryDao.getDislikedCount(),
            neutral = swipeHistoryDao.getNeutralCount()
        )
    }

    // SEARCH

    /*
    helper function to weigh the relevance of search result and order
     */
    fun Movie.weightedRating(minimumVotes: Int = 1000, averageMovieRatingConstant: Double = 6.8): Double {
        val numberOfVotes = this.voteCount
        val averageRating = this.voteAverage

        return (numberOfVotes.toDouble() / (numberOfVotes + minimumVotes)) *
                averageRating + (minimumVotes.toDouble() /
                (numberOfVotes + minimumVotes)) * averageMovieRatingConstant
    }

    /*
    relevance score
     */
    fun combinedScore(
        movie: Movie,
        pageIndex: Int,
        posInPage: Int,
        relevanceWeight: Double = 0.2,
        ratingWeight: Double = 0.3,
        popularityWeight: Double = 0.5
    ): Double {
        // Higher relevance = higher score
        val relevanceScore = 1.0 / ((pageIndex * 20) + posInPage + 1)

        val wr = movie.weightedRating()
        val pop = movie.popularity

        return (relevanceWeight * relevanceScore) +
                (ratingWeight * wr) +
                (popularityWeight * pop)
    }


    suspend fun searchMovies(query: String): Result<List<Movie>> {
        return try {
            // Search across multiple pages for better results
            val moviesWithMeta = mutableListOf<MovieRelevance>()

            for (page in 1..3) {
                val response = api.searchMovies(apiKey, query, page = page)
                val movies = response.body()?.results ?: emptyList()

                for ((index, movie) in movies.withIndex()){
                    moviesWithMeta.add(
                        MovieRelevance(
                            movie = movie,
                            pageIndex = page - 1,
                            positionInPage = index
                        )
                    )
                }

                // Stop if we got less than 20 results (last page)
                if (movies.size < 20) break
            }

            val sorted = moviesWithMeta.sortedByDescending { meta ->
                combinedScore(
                    movie = meta.movie,
                    pageIndex =  meta.pageIndex,
                    posInPage = meta.positionInPage
                )
            }

            Result.success(sorted.map { it.movie })
//            Result.success(moviesWithMeta.map{it.movie})

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // NATURAL LANGUAGE SEARCH

    suspend fun searchMoviesNaturalLanguage(query: String): Result<NaturalLanguageSearchResult> {
        return try {
            android.util.Log.d("NaturalLanguageSearch", "===== NATURAL LANGUAGE SEARCH STARTED =====")
            android.util.Log.d("NaturalLanguageSearch", "User query: $query")

            // Step 1: Get keywords from Gemini
            android.util.Log.d("NaturalLanguageSearch", "\nStep 1: Calling Gemini API...")
            val geminiResult = geminiService.convertNaturalLanguageToKeywords(query)

            if (geminiResult.isFailure) {
                android.util.Log.e("NaturalLanguageSearch", "Gemini API failed: ${geminiResult.exceptionOrNull()?.message}")
                return Result.failure(geminiResult.exceptionOrNull() ?: Exception("Gemini API failed"))
            }

            val geminiResponse = geminiResult.getOrNull()!!
            android.util.Log.d("NaturalLanguageSearch", "Gemini keywords: ${geminiResponse.keywords}")
            android.util.Log.d("NaturalLanguageSearch", "Gemini genres: ${geminiResponse.genres}")
            android.util.Log.d("NaturalLanguageSearch", "Gemini vibes: ${geminiResponse.vibes}")

            // Step 2: Convert genre names to TMDB genre IDs
            val genreIds = geminiResponse.genres.mapNotNull { genreName ->
                getGenreIdFromName(genreName)
            }
            android.util.Log.d("NaturalLanguageSearch", "\nStep 2: Mapped genre IDs: $genreIds")

            // Step 3: Search TMDB for matching keywords
            android.util.Log.d("NaturalLanguageSearch", "\nStep 3: Searching TMDB keywords...")
            val allKeywords = geminiResponse.keywords + geminiResponse.vibes
            val matchedKeywords = mutableListOf<TMDbKeyword>()

            for (keyword in allKeywords) {
                try {
                    val response = api.searchKeywords(apiKey, keyword)
                    val keywords = response.body()?.results ?: emptyList()

                    if (keywords.isNotEmpty()) {
                        android.util.Log.d("NaturalLanguageSearch", "  ✓ Found TMDB keyword for '$keyword': ${keywords[0].name} (ID: ${keywords[0].id})")
                        matchedKeywords.add(keywords[0])
                    } else {
                        android.util.Log.d("NaturalLanguageSearch", "  ✗ No TMDB keyword found for '$keyword'")
                    }
                } catch (e: Exception) {
                    android.util.Log.w("NaturalLanguageSearch", "  ✗ Error searching keyword '$keyword': ${e.message}")
                }
            }

            android.util.Log.d("NaturalLanguageSearch", "\nMatched ${matchedKeywords.size} TMDB keywords")

            // Step 4: Query TMDB discover endpoint
            android.util.Log.d("NaturalLanguageSearch", "\nStep 4: Fetching movies from TMDB...")
            val movies = mutableListOf<Movie>()

            // Build query parameters - Use COMMA for OR logic in keywords
            // Select top 3 most relevant keywords to avoid over-filtering
            val topKeywords = matchedKeywords.take(3)
            val keywordIds = topKeywords.map { it.id }.joinToString(",")  // COMMA = OR
            val genreIdsString = genreIds.joinToString(",")  // COMMA = OR

            android.util.Log.d("NaturalLanguageSearch", "Query parameters:")
            android.util.Log.d("NaturalLanguageSearch", "  - Using top ${topKeywords.size} keywords: ${topKeywords.map { it.name }}")
            android.util.Log.d("NaturalLanguageSearch", "  - Keyword IDs: $keywordIds")
            android.util.Log.d("NaturalLanguageSearch", "  - Genre IDs: $genreIdsString")

            // Fetch multiple pages for better results
            for (page in 1..5) {  // Increased from 3 to 5 pages
                try {
                    val response = api.discoverByKeywords(
                        apiKey = apiKey,
                        withKeywords = keywordIds.ifEmpty { null },
                        withGenres = genreIdsString.ifEmpty { null },
                        sortBy = "vote_average.desc",  // Sort by rating instead of popularity
                        page = page,
                        voteCountGte = 100 // Increased from 50 to get more established films
                    )

                    val pageMovies = response.body()?.results ?: emptyList()
                    movies.addAll(pageMovies)

                    android.util.Log.d("NaturalLanguageSearch", "  Page $page: ${pageMovies.size} movies")

                    if (pageMovies.size < 20) break
                } catch (e: Exception) {
                    android.util.Log.e("NaturalLanguageSearch", "Error fetching page $page: ${e.message}")
                    break
                }
            }

            // Fallback: If no movies found with keywords, try genre-only search
            if (movies.isEmpty() && genreIds.isNotEmpty()) {
                android.util.Log.d("NaturalLanguageSearch", "\nFallback: No results with keywords, trying genre-only search...")

                for (page in 1..2) {
                    try {
                        val response = api.discoverByKeywords(
                            apiKey = apiKey,
                            withKeywords = null,
                            withGenres = genreIdsString,
                            sortBy = "popularity.desc",
                            page = page,
                            voteCountGte = 100
                        )

                        val pageMovies = response.body()?.results ?: emptyList()
                        movies.addAll(pageMovies)

                        android.util.Log.d("NaturalLanguageSearch", "  Fallback page $page: ${pageMovies.size} movies")

                        if (pageMovies.size < 20) break
                    } catch (e: Exception) {
                        android.util.Log.e("NaturalLanguageSearch", "Error in fallback page $page: ${e.message}")
                        break
                    }
                }
            }

            val uniqueMovies = movies.distinctBy { it.id }

            android.util.Log.d("NaturalLanguageSearch", "\n===== SEARCH COMPLETE =====")
            android.util.Log.d("NaturalLanguageSearch", "Total movies found: ${uniqueMovies.size}")

            if (uniqueMovies.isNotEmpty()) {
                android.util.Log.d("NaturalLanguageSearch", "Sample results:")
                uniqueMovies.take(5).forEach { movie ->
                    android.util.Log.d("NaturalLanguageSearch", "  - ${movie.title} (${movie.releaseDate.take(4)})")
                }
            }

            val result = NaturalLanguageSearchResult(
                movies = uniqueMovies,
                geminiResponse = geminiResponse,
                matchedKeywords = matchedKeywords,
                usedGenreIds = genreIds
            )

            Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("NaturalLanguageSearch", "Natural language search failed", e)
            Result.failure(e)
        }
    }

    private fun getGenreIdFromName(genreName: String): Int? {
        return when (genreName.lowercase()) {
            "action" -> 28
            "adventure" -> 12
            "animation" -> 16
            "comedy" -> 35
            "crime" -> 80
            "documentary" -> 99
            "drama" -> 18
            "family" -> 10751
            "fantasy" -> 14
            "history" -> 36
            "horror" -> 27
            "music" -> 10402
            "mystery" -> 9648
            "romance" -> 10749
            "science fiction", "sci-fi" -> 878
            "tv movie" -> 10770
            "thriller" -> 53
            "war" -> 10752
            "western" -> 37
            else -> null
        }
    }

    //  FAVORITES

//    fun checkMovieCached(): Flow<>

    fun getLikedMovies(): Flow<List<SwipeHistoryEntity>> {
        return swipeHistoryDao.getLikedMovies()
    }

    fun getDislikedMovies(): Flow<List<SwipeHistoryEntity>> {
        return swipeHistoryDao.getDislikedMovies()
    }

    suspend fun addFavorite(movieId: Int) {
        favoriteDao.addFavorite(FavoriteEntity(movieId))
    }

    suspend fun removeFavorite(movieId: Int) {
        favoriteDao.removeFavorite(movieId)
    }

    suspend fun toggleFavorite(movieId: Int): Boolean {
        return if (favoriteDao.isFavorite(movieId)) {
            favoriteDao.removeFavorite(movieId)
            false
        } else {
            favoriteDao.addFavorite(FavoriteEntity(movieId))
            true
        }
    }

    fun getAllFavorites(): Flow<List<Movie>> {
        return favoriteDao.getAllFavorites().map { favorites ->
            favorites.mapNotNull { favorite ->
                movieDao.getMovieById(favorite.movieId)?.toMovie()?.apply {
                    isFavorite = true
                }
            }
        }
    }

    suspend fun isFavorite(movieId: Int): Boolean {
        return favoriteDao.isFavorite(movieId)
    }

    // RECOMMENDATION SYSTEM WITH DETAILED LOGGING

    /**
     * Gets personalized movie recommendations based on swipe history
     * Analyzes liked movies to determine genre preferences, rating preferences, and year preferences
     */
    suspend fun getRecommendedMovies(): Result<List<Movie>> {
        return try {
            android.util.Log.d("RecommendationEngine", "═══════════════════════════════════════")
            android.util.Log.d("RecommendationEngine", "🎬 STARTING RECOMMENDATION ENGINE 🎬")
            android.util.Log.d("RecommendationEngine", "═══════════════════════════════════════")

            // Get all liked movies from swipe history
            val likedSwipes = swipeHistoryDao.getLikedMovies().first()

            android.util.Log.d("RecommendationEngine", "STEP 1: Analyzing Swipe History")
            android.util.Log.d("RecommendationEngine", "   ├─ Total liked swipes: ${likedSwipes.size}")

            if (likedSwipes.isEmpty()) {
                android.util.Log.w("RecommendationEngine", "   └─ INSUFFICIENT DATA: No liked swipes found")
                return Result.failure(Exception("Not enough swipe data. Like at least 5 movies to get recommendations!"))
            }

            // Get full movie details for liked movies
            val likedMovies = likedSwipes.mapNotNull { swipe ->
                movieDao.getMovieById(swipe.movieId)?.toMovie()
            }

            if (likedMovies.size < 3) {
                android.util.Log.w("RecommendationEngine", "   └─ INSUFFICIENT DATA: Only ${likedMovies.size} liked movies found (need 3+)")
                return Result.failure(Exception("Need at least 3 liked movies for recommendations"))
            }

            android.util.Log.d("RecommendationEngine", "   └─ Successfully loaded ${likedMovies.size} liked movie details")

            // ANALYZE GENRE PREFERENCES
            android.util.Log.d("RecommendationEngine", "\n🎭 STEP 2: Genre Analysis")

            val genreFrequency = mutableMapOf<Int, Int>()
            var moviesWithGenres = 0

            likedMovies.forEach { movie ->
                val genres = movie.genreIds ?: emptyList()
                if (genres.isNotEmpty()) {
                    moviesWithGenres++
                    genres.forEach { genreId ->
                        genreFrequency[genreId] = (genreFrequency[genreId] ?: 0) + 1
                    }
                }
            }

            android.util.Log.d("RecommendationEngine", "   ├─ Movies with genre data: $moviesWithGenres / ${likedMovies.size}")

            val topGenres = genreFrequency.entries
                .sortedByDescending { it.value }
                .take(3)
                .map { it.key }

            val genreFilter = if (topGenres.isNotEmpty()) {
                topGenres.joinToString("|")
            } else null

            android.util.Log.d("RecommendationEngine", "   ├─ Genre frequency distribution:")
            genreFrequency.entries
                .sortedByDescending { it.value }
                .take(5)
                .forEachIndexed { index, entry ->
                    val genreName = getGenreName(entry.key)
                    val percentage = (entry.value.toFloat() / moviesWithGenres * 100).toInt()
                    val isSelected = index < 3
                    val marker = if (isSelected) "✓" else " "
                    android.util.Log.d("RecommendationEngine", "   │  $marker Genre ${entry.key} ($genreName): ${entry.value} occurrences ($percentage%)")
                }

            android.util.Log.d("RecommendationEngine", "   ├─ Top 3 selected genres: ${topGenres.joinToString(", ") { "$it (${getGenreName(it)})" }}")
            android.util.Log.d("RecommendationEngine", "   └─ Genre filter string: $genreFilter")

            // ANALYZE RATING PREFERENCES
            android.util.Log.d("RecommendationEngine", "\n STEP 3: Rating Analysis")

            val ratings = likedMovies.map { it.voteAverage }
            val avgRating = ratings.average()
            val minRating = (avgRating - 1.0).coerceAtLeast(5.0)
            val maxRating = ratings.maxOrNull() ?: 10.0

            android.util.Log.d("RecommendationEngine", "   ├─ Rating distribution:")
            ratings.sorted().forEach { rating ->
                val stars = "★".repeat((rating / 2).toInt()) + "☆".repeat(5 - (rating / 2).toInt())
                android.util.Log.d("RecommendationEngine", "   │  $stars ${"%.1f".format(rating)}")
            }
            android.util.Log.d("RecommendationEngine", "   ├─ Average rating: ${"%.2f".format(avgRating)}")
            android.util.Log.d("RecommendationEngine", "   ├─ Minimum threshold: ${"%.2f".format(minRating)}")
            android.util.Log.d("RecommendationEngine", "   └─ Maximum rating: ${"%.2f".format(maxRating)}")

            // ANALYZE YEAR PREFERENCES
            android.util.Log.d("RecommendationEngine", "\n STEP 4: Year/Era Preference Analysis")

            val years = likedMovies.mapNotNull { movie ->
                movie.releaseDate.take(4).toIntOrNull()
            }

            val recentMovies = years.count { it >= 2020 }
            val modernMovies = years.count { it in 2010..2019 }
            val classicMovies = years.count { it < 2010 }

            val prefersRecent = recentMovies.toFloat() / likedMovies.size >= 0.6f
            val prefersModern = modernMovies.toFloat() / likedMovies.size >= 0.5f

            android.util.Log.d("RecommendationEngine", "   ├─ Era distribution:")
            android.util.Log.d("RecommendationEngine", "   │  Recent (2020+):  $recentMovies movies (${(recentMovies.toFloat() / years.size * 100).toInt()}%)")
            android.util.Log.d("RecommendationEngine", "   │  Modern (2010-19): $modernMovies movies (${(modernMovies.toFloat() / years.size * 100).toInt()}%)")
            android.util.Log.d("RecommendationEngine", "   │  Classic (<2010):  $classicMovies movies (${(classicMovies.toFloat() / years.size * 100).toInt()}%)")

            val releaseDateFrom = when {
                prefersRecent -> "2020-01-01"
                prefersModern -> "2010-01-01"
                else -> "2000-01-01"
            }
            val releaseDateTo = "2025-12-31"

            val eraPreference = when {
                prefersRecent -> "RECENT (2020+)"
                prefersModern -> "MODERN (2010-2019)"
                else -> "MIXED (2000+)"
            }

            android.util.Log.d("RecommendationEngine", "   ├─ Detected preference: $eraPreference")
            android.util.Log.d("RecommendationEngine", "   └─ Date filter: $releaseDateFrom to $releaseDateTo")

            // ========== CALCULATE WEIGHTS ==========
            android.util.Log.d("RecommendationEngine", "\n⚖️  STEP 5: Multi-Factor Weighting")
            android.util.Log.d("RecommendationEngine", "   ├─ Factor 1: Genre matching (HIGHEST priority)")
            android.util.Log.d("RecommendationEngine", "   │  Weight: 70%")
            android.util.Log.d("RecommendationEngine", "   │  Filter: with_genres=$genreFilter")
            android.util.Log.d("RecommendationEngine", "   │")
            android.util.Log.d("RecommendationEngine", "   ├─ Factor 2: Rating threshold (MEDIUM priority)")
            android.util.Log.d("RecommendationEngine", "   │  Weight: 20%")
            android.util.Log.d("RecommendationEngine", "   │  Filter: vote_average.gte=${"%.2f".format(minRating)}")
            android.util.Log.d("RecommendationEngine", "   │")
            android.util.Log.d("RecommendationEngine", "   ├─ Factor 3: Era preference (LOW priority)")
            android.util.Log.d("RecommendationEngine", "   │  Weight: 10%")
            android.util.Log.d("RecommendationEngine", "   │  Filter: release_date=$releaseDateFrom to $releaseDateTo")
            android.util.Log.d("RecommendationEngine", "   │")
            android.util.Log.d("RecommendationEngine", "   └─ Quality filters:")
            android.util.Log.d("RecommendationEngine", "      ├─ vote_count.gte=100 (exclude obscure movies)")
            android.util.Log.d("RecommendationEngine", "      └─ original_language=en (English movies)")


            android.util.Log.d("RecommendationEngine", "\n STEP 6: Fetching Recommendations from TMDb API")

            val allRecommendations = mutableListOf<Movie>()
            val pageCount = 5

            android.util.Log.d("RecommendationEngine", "   ├─ API Call Parameters:")
            android.util.Log.d("RecommendationEngine", "   │  Endpoint: /discover/movie")
            android.util.Log.d("RecommendationEngine", "   │  with_genres: $genreFilter")
            android.util.Log.d("RecommendationEngine", "   │  vote_average.gte: $minRating")
            android.util.Log.d("RecommendationEngine", "   │  primary_release_date.gte: $releaseDateFrom")
            android.util.Log.d("RecommendationEngine", "   │  primary_release_date.lte: $releaseDateTo")
            android.util.Log.d("RecommendationEngine", "   │  vote_count.gte: 100")
            android.util.Log.d("RecommendationEngine", "   │  original_language: en")
            android.util.Log.d("RecommendationEngine", "   │  sort_by: vote_average.desc")
            android.util.Log.d("RecommendationEngine", "   │  pages: $pageCount")
            android.util.Log.d("RecommendationEngine", "   │")

            for (page in 1..pageCount) {
                val response = api.discoverMovies(
                    apiKey = apiKey,
                    withGenres = genreFilter,
                    minRating = minRating,
                    releaseDateFrom = releaseDateFrom,
                    releaseDateTo = releaseDateTo,
                    voteCountGte = 100,
                    originalLanguage = "en",
                    sortBy = "vote_average.desc",
                    page = page
                )
                val movies = response.body()?.results ?: emptyList()
                allRecommendations.addAll(movies)
                android.util.Log.d("RecommendationEngine", "   │  Page $page: ${movies.size} movies fetched")
            }

            android.util.Log.d("RecommendationEngine", "   └─ Total fetched: ${allRecommendations.size} movies")


            android.util.Log.d("RecommendationEngine", "\n STEP 7: Filtering Results")

            val swipedIds = swipeHistoryDao.getAllSwipedMovieIds()
            android.util.Log.d("RecommendationEngine", "   ├─ Total swiped movies: ${swipedIds.size}")

            val unseenRecommendations = allRecommendations.filter { it.id !in swipedIds }
            android.util.Log.d("RecommendationEngine", "   ├─ Unseen movies: ${unseenRecommendations.size}")

            val finalRecommendations = unseenRecommendations.take(60)
            android.util.Log.d("RecommendationEngine", "   └─ Final recommendations: ${finalRecommendations.size} (capped at 60)")

            // SUMMARY
            android.util.Log.d("RecommendationEngine", "\nRECOMMENDATION SUMMARY")
            android.util.Log.d("RecommendationEngine", "═══════════════════════════════════════")
            android.util.Log.d("RecommendationEngine", "✓ Based on: ${likedMovies.size} liked movies")
            android.util.Log.d("RecommendationEngine", "✓ Top genres: ${topGenres.joinToString(", ") { getGenreName(it) }}")
            android.util.Log.d("RecommendationEngine", "✓ Rating preference: ${"%.1f".format(avgRating)}+ stars")
            android.util.Log.d("RecommendationEngine", "✓ Era preference: $eraPreference")
            android.util.Log.d("RecommendationEngine", "✓ Results: ${finalRecommendations.size} personalized recommendations")
            android.util.Log.d("RecommendationEngine", "═══════════════════════════════════════")

            if (finalRecommendations.isNotEmpty()) {
                android.util.Log.d("RecommendationEngine", "\n🎬 Sample Recommendations (Top 5):")
                finalRecommendations.take(5).forEachIndexed { index, movie ->
                    android.util.Log.d("RecommendationEngine", "   ${index + 1}. ${movie.title} (${movie.releaseDate.take(4)}) - ⭐ ${movie.voteAverage}")
                }
            }

            android.util.Log.d("RecommendationEngine", "\nRECOMMENDATION ENGINE COMPLETE\n")

            Result.success(finalRecommendations)
        } catch (e: Exception) {
            android.util.Log.e("RecommendationEngine", "\nERROR IN RECOMMENDATION ENGINE")
            android.util.Log.e("RecommendationEngine", "   Exception: ${e.message}", e)
            Result.failure(e)
        }
    }


    private fun getGenreName(genreId: Int): String {
        return when (genreId) {
            28 -> "Action"
            12 -> "Adventure"
            16 -> "Animation"
            35 -> "Comedy"
            80 -> "Crime"
            99 -> "Documentary"
            18 -> "Drama"
            10751 -> "Family"
            14 -> "Fantasy"
            36 -> "History"
            27 -> "Horror"
            10402 -> "Music"
            9648 -> "Mystery"
            10749 -> "Romance"
            878 -> "Science Fiction"
            10770 -> "TV Movie"
            53 -> "Thriller"
            10752 -> "War"
            37 -> "Western"
            else -> "Unknown($genreId)"
        }
    }

    suspend fun getMovieTrailers(movieId: Int): Result<List<Video>> {
        return try {
            android.util.Log.d("MovieRepository", "getMovieTrailers: Fetching for movie $movieId")

            val response = api.getMovieVideos(movieId, apiKey)

            if (response.isSuccessful) {
                val videos = response.body()?.results ?: emptyList()

                // Filter and sort trailers
                val trailers = videos
                    .filter { it.isYouTube() }  // Only YouTube videos
                    .sortedByDescending { it.getPriority() }  // Best first

                android.util.Log.d("MovieRepository", "getMovieTrailers: Found ${trailers.size} YouTube trailers")

                if (trailers.isNotEmpty()) {
                    android.util.Log.d("MovieRepository", "Top trailer: ${trailers[0].name} (${trailers[0].type}, official=${trailers[0].official})")
                }

                Result.success(trailers)
            } else {
                android.util.Log.e("MovieRepository", "getMovieTrailers: API error ${response.code()}")
                Result.failure(Exception("Failed to fetch trailers: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("MovieRepository", "getMovieTrailers: Exception - ${e.message}", e)
            Result.failure(e)
        }
    }
}

data class SwipeStats(
    val totalSwipes: Int,
    val liked: Int,
    val disliked: Int,
    val neutral: Int = 0
)