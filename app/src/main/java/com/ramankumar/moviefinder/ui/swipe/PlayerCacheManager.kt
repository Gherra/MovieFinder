package com.ramankumar.moviefinder.ui.swipe

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

/**
 * Manages a cache of 3 ExoPlayer instances for instant swipe transitions
 * Maintains players for: previous, current, and next movies
 */
class PlayerCacheManager(private val context: Context) {

    private val playerCache = mutableMapOf<Int, ExoPlayer>()
    private val maxCacheSize = 3

    /**
     * Get or create a player for the given movie index
     */
    fun getOrCreatePlayer(
        movieIndex: Int,
        youtubeKey: String?,
        autoPlay: Boolean = false
    ): ExoPlayer? {
        if (youtubeKey == null) return null

        return playerCache.getOrPut(movieIndex) {
            createPlayer(youtubeKey, autoPlay)
        }
    }

    /**
     * Prepare players for current and adjacent indices
     * This ensures instant playback when swiping
     */
    fun prepareAdjacentPlayers(
        currentIndex: Int,
        getYoutubeKey: (Int) -> String?
    ) {
        // Indices to maintain: previous, current, next
        val indicesToCache = listOf(
            currentIndex - 1,
            currentIndex,
            currentIndex + 1
        )

        // Create/update players for these indices
        indicesToCache.forEach { index ->
            if (index >= 0) {
                val youtubeKey = getYoutubeKey(index)
                if (youtubeKey != null && !playerCache.containsKey(index)) {
                    playerCache[index] = createPlayer(youtubeKey, autoPlay = (index == currentIndex))
                }
            }
        }

        // Clean up players outside the cache range
        val indicesToRemove = playerCache.keys.filter { it !in indicesToCache }
        indicesToRemove.forEach { index ->
            playerCache[index]?.release()
            playerCache.remove(index)
        }
    }

    /**
     * Pause all players except the specified index
     */
    fun pauseAllExcept(currentIndex: Int) {
        playerCache.forEach { (index, player) ->
            if (index != currentIndex) {
                player.pause()
            }
        }
    }

    /**
     * Play the player at the specified index
     */
    fun playAt(index: Int) {
        playerCache[index]?.let { player ->
            player.playWhenReady = true
        }
    }

    /**
     * Get player at index (if it exists)
     */
    fun getPlayer(index: Int): ExoPlayer? {
        return playerCache[index]
    }

    /**
     * Release all players and clear cache
     */
    fun releaseAll() {
        playerCache.values.forEach { it.release() }
        playerCache.clear()
    }

    private fun createPlayer(youtubeKey: String, autoPlay: Boolean): ExoPlayer {
        return ExoPlayer.Builder(context).build().apply {
            val videoUrl = getYouTubeVideoUrl(youtubeKey)
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = autoPlay
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f // Start muted for better UX
        }
    }

    private fun getYouTubeVideoUrl(key: String): String {
        // Note: Direct YouTube URLs may not work due to restrictions
        // For production, consider:
        // 1. Using YouTube extraction libraries (yt-dlp, youtube-dl)
        // 2. Backend service to fetch stream URLs
        // 3. Using TMDB video files if available
        return "https://www.youtube.com/watch?v=$key"
    }
}
