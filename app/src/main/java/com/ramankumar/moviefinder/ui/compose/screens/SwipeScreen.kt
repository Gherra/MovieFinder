package com.ramankumar.moviefinder.ui.compose.screens

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ramankumar.moviefinder.model.Movie
import com.ramankumar.moviefinder.ui.compose.components.LoadingIndicator
import com.ramankumar.moviefinder.ui.compose.theme.Gold
import com.ramankumar.moviefinder.ui.compose.theme.Red
import com.ramankumar.moviefinder.ui.swipe.SwipeViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeScreen(
    viewModel: SwipeViewModel,
    onBackClick: () -> Unit
) {
    val movies by viewModel.movies.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val swipeStats by viewModel.swipeStats.collectAsState()
    val showResetDialog by viewModel.showResetDialog.collectAsState()
    val showInfoDialog by viewModel.showInfoDialog.collectAsState()

    val currentMovie = movies.getOrNull(currentIndex)

    // Swipe state
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val screenWidth = LocalConfiguration.current.screenWidthDp *
            LocalConfiguration.current.densityDpi / 160f
    val screenHeight = LocalConfiguration.current.screenHeightDp *
            LocalConfiguration.current.densityDpi / 160f
    val swipeThresholdX = screenWidth * 0.5f  // ← INCREASED from 0.4f (less sensitive)
    val swipeThresholdY = screenHeight * 0.4f  // ← INCREASED from 0.3f

    val scope = rememberCoroutineScope()

    // Reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideResetDialog() },
            title = { Text("Reset Swipe History?") },
            text = { Text("This will clear all your swipe history and show you all movies again. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.resetSwipeHistory() },
                    colors = ButtonDefaults.textButtonColors(contentColor = Red)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideResetDialog() }) {
                    Text("Cancel")
                }
            },
            containerColor = Color(0xFF1F1F1F),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFCCCCCC)
        )
    }
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideInfoDialog()},
            title = { Text("Swipe Details:")},
            text = { Text("⬆\uFE0F Swipe up if you're not sure\n➡\uFE0F Swipe right to like \n⬅\uFE0F Swipe left to pass")},
            confirmButton = {
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.hideInfoDialog() },
                    colors = ButtonDefaults.textButtonColors(contentColor = Red)
                ) {
                    Text("Finished")
                }
            },
            containerColor = Color(0xFF1F1F1F),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFCCCCCC)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Swipe Movies") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showInfoDialog()}) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = "Info",
                            tint = Red
                        )
                    }
                    // Reset history button
                    IconButton(onClick = { viewModel.showResetDialog() }) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Reset History",
                            tint = Red
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1F1F1F),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black,
        bottomBar = {
            // Swipe stats at bottom
            swipeStats?.let { stats ->
                Surface(
                    color = Color(0xFF1F1F1F),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Liked
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${stats.liked}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold
                            )
                            Text(
                                text = "❤️ Liked",
                                fontSize = 12.sp,
                                color = Color(0xFF888888)
                            )
                        }

                        // Not Sure
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${stats.neutral}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFAA00)
                            )
                            Text(
                                text = "🤔 Not Sure",
                                fontSize = 12.sp,
                                color = Color(0xFF888888)
                            )
                        }

                        // Passed
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${stats.disliked}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF888888)
                            )
                            Text(
                                text = "👎 Passed",
                                fontSize = 12.sp,
                                color = Color(0xFF888888)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            when {
                isLoading -> LoadingIndicator()

                error != null -> {
                    Text(
                        text = error ?: "Error loading movies",
                        color = Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp)
                    )
                }

                currentMovie != null -> {
                    SwipeCard(
                        movie = currentMovie,
                        offsetX = offsetX,
                        offsetY = offsetY,
                        onDrag = { dragAmount ->
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        },
                        onDragEnd = {
                            // Check swipe up first (negative Y = up)
                            if (offsetY < -swipeThresholdY && abs(offsetX) < swipeThresholdX) {
                                scope.launch {
                                    viewModel.onSwipeUp()
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                            // Check horizontal swipe
                            else if (abs(offsetX) > swipeThresholdX) {
                                scope.launch {
                                    if (offsetX > 0) {
                                        viewModel.onSwipeRight()
                                    } else {
                                        viewModel.onSwipeLeft()
                                    }
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                            // Reset if no threshold met
                            else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        },
                        currentIndex = currentIndex + 1,
                        totalMovies = movies.size
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉 No more movies!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tap the refresh button above to reset history",
                            fontSize = 14.sp,
                            color = Color(0xFF888888)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeCard(
    movie: Movie,
    offsetX: Float,
    offsetY: Float,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    currentIndex: Int,
    totalMovies: Int
) {
    val rotation = (offsetX / 20f).coerceIn(-30f, 30f)
    val alpha = (1f - (abs(offsetX) / 1000f) - (abs(offsetY) / 1000f)).coerceIn(0f, 1f)

    // Show hint colors based on swipe direction
    val hintColor = when {
        offsetY < -200 -> Color(0xFFFFAA00) // Orange for "not sure" (up)
        offsetX > 200 -> Gold // Gold for like (right)
        offsetX < -200 -> Color.Red // Red for pass (left)
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Counter in top right
        Text(
            text = "$currentIndex / $totalMovies",
            color = Color(0xFF888888),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )

        // Hint text overlay
        if (hintColor != Color.Transparent) {
            Text(
                text = when {
                    offsetY < -200 -> "NOT SURE 🤔"
                    offsetX > 200 -> "LIKE ❤️"
                    else -> "PASS 👎"
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = hintColor,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        }

        Card(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = offsetX
                    translationY = offsetY
                    rotationZ = rotation
                    this.alpha = alpha
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount)
                        },
                        onDragEnd = { onDragEnd() }
                    )
                },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1F1F1F)
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column {
                // Movie Poster
                AsyncImage(
                    model = movie.getPosterUrl(),
                    contentDescription = movie.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentScale = ContentScale.Crop
                )

                // Movie Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp)
                        .padding(20.dp)
                ) {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = movie.releaseDate.take(4),
                            color = Color(0xFF888888)
                        )

                        Text(
                            text = "⭐ %.1f".format(java.util.Locale.US, movie.voteAverage),
                            color = Gold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Overview",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = movie.overview,
                        color = Color(0xFFCCCCCC),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 4
                    )
                }
            }
        }
    }
}