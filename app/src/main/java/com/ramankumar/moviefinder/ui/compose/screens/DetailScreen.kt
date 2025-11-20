package com.ramankumar.moviefinder.ui.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ramankumar.moviefinder.model.Video
import com.ramankumar.moviefinder.ui.compose.components.TrailerItem
import com.ramankumar.moviefinder.ui.compose.theme.Gold
import com.ramankumar.moviefinder.ui.compose.theme.Red

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    movieId: Int,
    movieTitle: String,
    movieYear: String,
    movieRating: Double,
    movieOverview: String,
    movieBackdrop: String?,
    initialIsFavorite: Boolean,
    trailers: List<Video> = emptyList(),  // NEW
    isLoadingTrailers: Boolean = false,    // NEW
    onBackClick: () -> Unit,
    onFavoriteToggle: (Boolean) -> Unit,
    onTrailerClick: (String, String) -> Unit = { _, _ -> }  // NEW: (videoKey, videoTitle)
) {
    var isFavorite by remember { mutableStateOf(initialIsFavorite) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movie Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1F1F1F),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {

            if (movieBackdrop != null) {
                AsyncImage(
                    model = "https://image.tmdb.org/t/p/w780$movieBackdrop",
                    contentDescription = movieTitle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Movie Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Title
                Text(
                    text = movieTitle,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Year and Rating Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = movieYear,
                        fontSize = 18.sp,
                        color = Color(0xFF888888)
                    )

                    Text(
                        text = "⭐ %.1f/10".format(java.util.Locale.US, movieRating),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Favorite Button
                Button(
                    onClick = {
                        isFavorite = !isFavorite
                        onFavoriteToggle(isFavorite)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFavorite) Gold else Red
                    )
                ) {
                    Icon(
                        imageVector = if (isFavorite)
                            Icons.Filled.Favorite
                        else
                            Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFavorite)
                            "Remove from Favorites"
                        else
                            "Add to Favorites",
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Overview Title
                Text(
                    text = "Overview",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Overview Text
                Text(
                    text = movieOverview,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = Color(0xFFCCCCCC)
                )

                // NEW: Trailers Section
                Spacer(modifier = Modifier.height(24.dp))

                // Trailers Title
                Text(
                    text = "Trailers",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Loading State
                if (isLoadingTrailers) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Red
                        )
                    }
                }
                // Trailers List
                else if (trailers.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        trailers.take(5).forEach { trailer ->  // Show up to 5 trailers
                            TrailerItem(
                                trailer = trailer,
                                onClick = {
                                    onTrailerClick(trailer.key, trailer.name)
                                }
                            )
                        }
                    }
                }
                // No Trailers
                else {
                    Text(
                        text = "No trailers available",
                        fontSize = 14.sp,
                        color = Color(0xFF888888)
                    )
                }
            }
        }
    }
}