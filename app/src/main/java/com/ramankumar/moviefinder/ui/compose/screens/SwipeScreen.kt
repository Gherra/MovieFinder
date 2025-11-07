package com.ramankumar.moviefinder.ui.compose.screens

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ramankumar.moviefinder.model.Movie
import com.ramankumar.moviefinder.ui.compose.components.LoadingIndicator
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

    val currentMovie = viewModel.getCurrentMovie()

    // Swipe state
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val screenWidth = LocalConfiguration.current.screenWidthDp *
            LocalConfiguration.current.densityDpi / 160f
    val swipeThreshold = screenWidth * 0.4f

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Swipe Movies") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1F1F1F),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background instruction text
            Text(
                text = "Swipe right if you like it\nSwipe left to pass",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF444444),
                modifier = Modifier.align(Alignment.Center)
            )

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
                            if (abs(offsetX) > swipeThreshold) {
                                scope.launch {
                                    if (offsetX > 0) {
                                        viewModel.onSwipeRight()
                                    } else {
                                        viewModel.onSwipeLeft()
                                    }
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        },
                        currentIndex = currentIndex + 1,
                        totalMovies = movies.size
                    )
                }

                else -> {
                    Text(
                        text = "No more movies!",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
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
    val alpha = (1f - (abs(offsetX) / 1000f)).coerceIn(0f, 1f)

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

                // Movie Info (scrollable if needed)
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
                            text = "⭐ ${String.format("%.1f", movie.voteAverage)}",
                            color = Color(0xFFFFD700)
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

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "← Swipe to decide →",
                        color = Color(0xFF666666),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}