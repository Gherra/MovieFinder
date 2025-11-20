package com.ramankumar.moviefinder.model

import com.google.gson.annotations.SerializedName

/**
 * Response from TMDb /movie/{id}/videos endpoint
 */
data class VideoResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("results")
    val results: List<Video>
)

/**
 * Individual video (trailer/teaser/clip) from TMDb
 * Contains YouTube video key for playback
 */
data class Video(
    @SerializedName("id")
    val id: String,

    @SerializedName("key")
    val key: String,  // YouTube video ID (e.g., "dQw4w9WgXcQ")

    @SerializedName("name")
    val name: String,  // Trailer title (e.g., "Official Trailer")

    @SerializedName("site")
    val site: String,  // "YouTube" or "Vimeo"

    @SerializedName("type")
    val type: String,  // "Trailer", "Teaser", "Clip", "Featurette"

    @SerializedName("official")
    val official: Boolean = false,  // Official release vs fan-made

    @SerializedName("published_at")
    val publishedAt: String? = null,  // ISO 8601 date

    @SerializedName("size")
    val size: Int = 1080  // Video quality (360, 480, 720, 1080)
) {
    /**
     * Get YouTube thumbnail URL
     * Uses YouTube's direct thumbnail API (no key needed!)
     */
    fun getThumbnailUrl(): String {
        return "https://img.youtube.com/vi/$key/hqdefault.jpg"
    }

    /**
     * Check if this video is a proper trailer (not teaser/clip)
     */
    fun isTrailer(): Boolean {
        return type.equals("Trailer", ignoreCase = true)
    }

    /**
     * Check if video is on YouTube (vs Vimeo etc)
     */
    fun isYouTube(): Boolean {
        return site.equals("YouTube", ignoreCase = true)
    }

    /**
     * Get priority score for sorting
     * Higher = better (official trailers first)
     */
    fun getPriority(): Int {
        var priority = 0
        if (official) priority += 100
        if (isTrailer()) priority += 50
        else if (type.equals("Teaser", ignoreCase = true)) priority += 25
        return priority
    }
}