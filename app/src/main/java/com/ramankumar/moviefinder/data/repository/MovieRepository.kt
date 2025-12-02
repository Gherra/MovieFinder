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

            Result.success(allMovies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets shuffled movies for swiping, excluding already swiped ones
     * Fetches 10 pages (~200 movies) for variety
     */
    suspend fun getShuffledMovies(): Result<List<Movie>> {
        return try {

            // Fetch multiple pages for variety (10 pages = ~200 movies)
            val allMovies = mutableListOf<Movie>()
            for (page in 1..10) {
                val response = api.getPopularMovies(apiKey, page = page)
                val movies = response.body()?.results ?: emptyList()
                allMovies.addAll(movies)
            }

            // Get list of already swiped movie IDs
            val swipedIds = swipeHistoryDao.getAllSwipedMovieIds()

            // Filter out swiped movies
            val unswipedMovies = allMovies.filter { it.id !in swipedIds }

            if (unswipedMovies.isEmpty()) {
                // If all movies swiped, return all (user can reset history)
                val shuffled = allMovies.shuffled()
                return Result.success(shuffled)
            }

            val shuffledMovies = unswipedMovies.shuffled()

            Result.success(shuffledMovies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Records a swipe action (left = dislike, right = like, up = neutral)
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

    }

    /**
     * Clears all swipe history (for reset functionality)
     */
    suspend fun clearSwipeHistory() {
        swipeHistoryDao.deleteAllSwipes()
    }

    suspend fun getSwipeStats(): SwipeStats {
        return SwipeStats(
            totalSwipes = swipeHistoryDao.getTotalSwipeCount(),
            liked = swipeHistoryDao.getLikedCount(),
            disliked = swipeHistoryDao.getDislikedCount(),
            neutral = swipeHistoryDao.getNeutralCount()
        )
    }

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
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /*
    implement searching with natural language using Gemini API
    */
    suspend fun searchMoviesNaturalLanguage(query: String): Result<NaturalLanguageSearchResult> {
        return try {
            // Get keywords from Gemini
            val geminiResult = geminiService.convertNaturalLanguageToKeywords(query)

            if (geminiResult.isFailure) {
                return Result.failure(geminiResult.exceptionOrNull() ?: Exception("Gemini API failed"))
            }

            val geminiResponse = geminiResult.getOrNull()!!

            // Search for the reference movie if specified
            // (reference movie is the movie predicted by gemini)
            val referenceMovies = mutableListOf<Movie>()
            geminiResponse.referenceMovie?.let { movieTitle ->
                try {
                    val response = api.searchMovies(apiKey, movieTitle, page = 1)
                    val results = response.body()?.results ?: emptyList()
                    if (results.isNotEmpty()) {
                        referenceMovies.add(results[0])  // Add the top match
                    } else {
                        
                    }
                } catch (e: Exception) {
                }
            }

            // Convert genre names to TMDB genre IDs
            val genreIds = geminiResponse.genres.mapNotNull { genreName ->
                getGenreIdFromName(genreName)
            }

            // Search TMDB for matching keywords
            val allKeywords = geminiResponse.keywords + geminiResponse.vibes
            val matchedKeywords = mutableListOf<TMDbKeyword>()

            for (keyword in allKeywords) {
                try {
                    val response = api.searchKeywords(apiKey, keyword)
                    val keywords = response.body()?.results ?: emptyList()

                    if (keywords.isNotEmpty()) {
                        matchedKeywords.add(keywords[0])
                    } else {
                    }
                } catch (e: Exception) {
                }
            }


            // Query TMDB discover endpoint
            val movies = mutableListOf<Movie>()

            // Use COMMA for OR logic in keywords
            // Select top 3 most relevant keywords to avoid over-filtering
            val topKeywords = matchedKeywords.take(3)
            val keywordIds = topKeywords.map { it.id }.joinToString(",")  // COMMA = OR
            val genreIdsString = genreIds.joinToString(",")  // COMMA = OR

            // Fetch multiple pages for better results
            for (page in 1..5) { 
                try {
                    val response = api.discoverByKeywords(
                        apiKey = apiKey,
                        withKeywords = keywordIds.ifEmpty { null },
                        withGenres = genreIdsString.ifEmpty { null },
                        sortBy = "vote_average.desc",  // Sort by rating instead of popularity
                        page = page,
                        voteCountGte = 100
                    )

                    val pageMovies = response.body()?.results ?: emptyList()
                    movies.addAll(pageMovies)

                    if (pageMovies.size < 20) break
                } catch (e: Exception) {
                    break
                }
            }

            // Fallback: If no movies found with keywords, try genre-only search
            if (movies.isEmpty() && genreIds.isNotEmpty()) {

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

                        if (pageMovies.size < 20) break
                    } catch (e: Exception) {
                        break
                    }
                }
            }

            // Combine reference movies with keyword-based movies 
            // Remove duplicates by movie ID - reference movie takes priority
            val combinedMovies = (referenceMovies + movies).distinctBy { it.id }

            val result = NaturalLanguageSearchResult(
                movies = combinedMovies,
                geminiResponse = geminiResponse,
                matchedKeywords = matchedKeywords,
                usedGenreIds = genreIds
            )

            Result.success(result)
        } catch (e: Exception) {
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

    /**
     * Gets personalized movie recommendations based on swipe history
     * Analyzes liked movies to determine genre preferences, rating preferences, and year preferences
     */
    suspend fun getRecommendedMovies(): Result<List<Movie>> {
        return try {

            // Get all liked movies from swipe history
            val likedSwipes = swipeHistoryDao.getLikedMovies().first()

            if (likedSwipes.isEmpty()) {
                return Result.failure(Exception("Not enough swipe data. Like at least 5 movies to get recommendations!"))
            }

            // Get full movie details for liked movies
            val likedMovies = likedSwipes.mapNotNull { swipe ->
                movieDao.getMovieById(swipe.movieId)?.toMovie()
            }

            if (likedMovies.size < 3) {
                return Result.failure(Exception("Need at least 3 liked movies for recommendations"))
            }

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

            val topGenres = genreFrequency.entries
                .sortedByDescending { it.value }
                .take(3)
                .map { it.key }

            val genreFilter = if (topGenres.isNotEmpty()) {
                topGenres.joinToString("|")
            } else null

            genreFrequency.entries
                .sortedByDescending { it.value }
                .take(5)
                .forEachIndexed { index, entry ->
                    val genreName = getGenreName(entry.key)
                    val percentage = (entry.value.toFloat() / moviesWithGenres * 100).toInt()
                    val isSelected = index < 3
                    val marker = if (isSelected) "✓" else " "
                }

            val ratings = likedMovies.map { it.voteAverage }
            val avgRating = ratings.average()
            val minRating = (avgRating - 1.0).coerceAtLeast(5.0)
            val maxRating = ratings.maxOrNull() ?: 10.0

            ratings.sorted().forEach { rating ->
                val stars = "★".repeat((rating / 2).toInt()) + "☆".repeat(5 - (rating / 2).toInt())
            }

            val years = likedMovies.mapNotNull { movie ->
                movie.releaseDate.take(4).toIntOrNull()
            }

            val recentMovies = years.count { it >= 2020 }
            val modernMovies = years.count { it in 2010..2019 }
            val classicMovies = years.count { it < 2010 }

            val prefersRecent = recentMovies.toFloat() / likedMovies.size >= 0.6f
            val prefersModern = modernMovies.toFloat() / likedMovies.size >= 0.5f

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

            val allRecommendations = mutableListOf<Movie>()
            val pageCount = 5

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
            }

            val swipedIds = swipeHistoryDao.getAllSwipedMovieIds()

            val unseenRecommendations = allRecommendations.filter { it.id !in swipedIds }

            val finalRecommendations = unseenRecommendations.take(60)

            Result.success(finalRecommendations)
        } catch (e: Exception) {
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
            val response = api.getMovieVideos(movieId, apiKey)

            if (response.isSuccessful) {
                val videos = response.body()?.results ?: emptyList()

                // Filter and sort trailers
                val trailers = videos
                    .filter { it.isYouTube() }  // Only YouTube videos
                    .sortedByDescending { it.getPriority() }  // Best first

                Result.success(trailers)
            } else {
                Result.failure(Exception("Failed to fetch trailers: ${response.code()}"))
            }
        } catch (e: Exception) {
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