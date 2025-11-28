package com.ramankumar.moviefinder.ui.trailer

import android.app.PictureInPictureParams
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.ramankumar.moviefinder.ui.compose.theme.MovieFinderTheme

/**
 * Trailer Player Activity
 * Version 13.0.0 - Simplified, working implementation
 * NO custom IFramePlayerOptions needed!
 */
class TrailerPlayerActivity : ComponentActivity() {

    private var youtubePlayerView: YouTubePlayerView? = null
    private var youtubePlayer: YouTubePlayer? = null
    private var currentVideoKey: String? = null
    private var isInPipMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentVideoKey = intent.getStringExtra("VIDEO_KEY")
        val videoTitle = intent.getStringExtra("VIDEO_TITLE") ?: "Trailer"

        if (currentVideoKey == null) {
            Toast.makeText(this,
                "Invalid trailer key! Please report!",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        setContent {
            MovieFinderTheme {
                TrailerPlayerScreen(
                    videoKey = currentVideoKey!!,
                    videoTitle = videoTitle,
                    onBackClick = { finish() },
                    onYouTubePlayerCreated = { player ->
                        youtubePlayer = player
                    },
                    onYouTubePlayerViewCreated = { playerView ->
                        youtubePlayerView = playerView
                    },
                    onOpenYouTubeApp = {
                        openInYouTubeApp(currentVideoKey!!)
                        finish()
                    }
                )
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                super.enterPictureInPictureMode(params)
            }
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode = isInPictureInPictureMode

        if (isInPictureInPictureMode) {
            android.util.Log.d("TrailerPlayer", "Entered PiP mode")
        } else {
            android.util.Log.d("TrailerPlayer", "Exited PiP mode")
        }
    }

    override fun onDestroy() {
        youtubePlayerView?.release()
        super.onDestroy()
    }

    private fun openInYouTubeApp(videoKey: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoKey"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            android.util.Log.d("TrailerPlayer", "Opened in YouTube app: $videoKey")
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoKey"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            android.util.Log.d("TrailerPlayer", "Opened in browser: $videoKey")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrailerPlayerScreen(
    videoKey: String,
    videoTitle: String,
    onBackClick: () -> Unit,
    onYouTubePlayerCreated: (YouTubePlayer) -> Unit,
    onYouTubePlayerViewCreated: (YouTubePlayerView) -> Unit,
    onOpenYouTubeApp: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var shouldRedirect by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(shouldRedirect) {
        if (shouldRedirect) {
            kotlinx.coroutines.delay(1500)
            onOpenYouTubeApp()
        }
    }

    if (!isLandscape) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = videoTitle,
                            color = Color.White,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black
                    )
                )
            },
            containerColor = Color.Black
        ) { paddingValues ->
            PlayerContent(
                videoKey = videoKey,
                isLoading = isLoading,
                errorMessage = errorMessage,
                shouldRedirect = shouldRedirect,
                paddingValues = paddingValues,
                lifecycleOwner = lifecycleOwner,
                onLoadingChange = { isLoading = it },
                onErrorChange = { errorMessage = it },
                onShouldRedirectChange = { shouldRedirect = it },
                onYouTubePlayerCreated = onYouTubePlayerCreated,
                onYouTubePlayerViewCreated = onYouTubePlayerViewCreated,
                onOpenYouTubeApp = onOpenYouTubeApp
            )
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            PlayerContent(
                videoKey = videoKey,
                isLoading = isLoading,
                errorMessage = errorMessage,
                shouldRedirect = shouldRedirect,
                paddingValues = PaddingValues(0.dp),
                lifecycleOwner = lifecycleOwner,
                onLoadingChange = { isLoading = it },
                onErrorChange = { errorMessage = it },
                onShouldRedirectChange = { shouldRedirect = it },
                onYouTubePlayerCreated = onYouTubePlayerCreated,
                onYouTubePlayerViewCreated = onYouTubePlayerViewCreated,
                onOpenYouTubeApp = onOpenYouTubeApp
            )
        }
    }
}

@Composable
private fun PlayerContent(
    videoKey: String,
    isLoading: Boolean,
    errorMessage: String?,
    shouldRedirect: Boolean,
    paddingValues: PaddingValues,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onLoadingChange: (Boolean) -> Unit,
    onErrorChange: (String?) -> Unit,
    onShouldRedirectChange: (Boolean) -> Unit,
    onYouTubePlayerCreated: (YouTubePlayer) -> Unit,
    onYouTubePlayerViewCreated: (YouTubePlayerView) -> Unit,
    onOpenYouTubeApp: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // YouTube Player - V13.0.0 SIMPLIFIED (NO IFramePlayerOptions!)
        AndroidView(
            factory = { context ->
                YouTubePlayerView(context).apply {
                    // Let library handle initialization automatically
                    enableAutomaticInitialization = true

                    // Add lifecycle observer
                    lifecycleOwner.lifecycle.addObserver(this)
                    onYouTubePlayerViewCreated(this)

                    // Add listener with default settings (NO custom options!)
                    addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            android.util.Log.d("TrailerPlayer", "✅ Player READY! Loading: $videoKey")
                            onYouTubePlayerCreated(youTubePlayer)
                            youTubePlayer.loadVideo(videoKey, 0f)
                            onLoadingChange(false)
                        }

                        override fun onError(
                            youTubePlayer: YouTubePlayer,
                            error: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError
                        ) {
                            android.util.Log.e("TrailerPlayer", "❌ ERROR: $error")
                            onLoadingChange(false)

                            // Only redirect if it's definitely the embed restriction
                            if (error == com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER) {
                                android.util.Log.w("TrailerPlayer", "🔄 Video not embeddable, will redirect to YouTube")
                                onErrorChange("This video can't be played in the app.\nOpening in YouTube...")
                                onShouldRedirectChange(true)
                            } else {
                                // Other errors - log but don't auto-redirect
                                android.util.Log.w("TrailerPlayer", "⚠️ Other error (not auto-redirecting): $error")
                                onErrorChange("Error loading video.\nTry opening in YouTube.")
                            }
                        }

                        override fun onStateChange(
                            youTubePlayer: YouTubePlayer,
                            state: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState
                        ) {
                            android.util.Log.d("TrailerPlayer", "📺 State: $state")

                            if (state == com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PLAYING) {
                                android.util.Log.d("TrailerPlayer", "🎬 VIDEO IS PLAYING! SUCCESS!")
                            }
                        }
                    })
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading Indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFE50914)
            )
        }

        // Error Message
        if (errorMessage != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1F1F1F)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    if (shouldRedirect) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = Color(0xFFE50914)
                        )
                    } else {
                        Button(
                            onClick = onOpenYouTubeApp,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE50914)
                            )
                        ) {
                            Text("Open in YouTube")
                        }
                    }
                }
            }
        }
    }

    // Handle lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    android.util.Log.d("TrailerPlayer", "Lifecycle: ON_PAUSE")
                }
                Lifecycle.Event.ON_RESUME -> {
                    android.util.Log.d("TrailerPlayer", "Lifecycle: ON_RESUME")
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}