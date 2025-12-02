package com.ramankumar.moviefinder.ui.compose.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

/**
 * YouTube IFrame-based trailer player for swipe cards
 * Uses the existing YouTube player library with lifecycle management
 * to prevent reloads on orientation changes
 */
@Composable
fun TrailerPlayer(
    youtubeKey: String?,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    onPlayerReady: ((YouTubePlayer) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    if (youtubeKey != null) {
        var youtubePlayer by remember { mutableStateOf<YouTubePlayer?>(null) }

        AndroidView(
            factory = { ctx ->
                YouTubePlayerView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // Enable automatic lifecycle management
                    lifecycleOwner.lifecycle.addObserver(this)

                    // Use addYouTubePlayerListener for automatic initialization
                    addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                        override fun onReady(player: YouTubePlayer) {
                            youtubePlayer = player

                            // Mute and load video
                            player.mute()
                            if (autoPlay) {
                                player.loadVideo(youtubeKey, 0f)
                            } else {
                                player.cueVideo(youtubeKey, 0f)
                            }

                            onPlayerReady?.invoke(player)
                        }
                    })
                }
            },
            update = { view ->
                // Only reload if the YouTube key changes
                if (youtubePlayer != null && autoPlay) {
                    youtubePlayer?.loadVideo(youtubeKey, 0f)
                }
            },
            modifier = modifier
        )

        // Pause video when lifecycle is paused
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> youtubePlayer?.pause()
                    Lifecycle.Event.ON_RESUME -> if (autoPlay) youtubePlayer?.play()
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    } else {
        // Placeholder when no trailer available
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color(0xFF1F1F1F))
        )
    }
}
