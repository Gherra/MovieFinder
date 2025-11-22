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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ramankumar.moviefinder.model.Video
import com.ramankumar.moviefinder.ui.compose.components.TrailerItem
import com.ramankumar.moviefinder.ui.compose.theme.DarkBackground
import com.ramankumar.moviefinder.ui.compose.theme.DarkSurface
import com.ramankumar.moviefinder.ui.compose.theme.Gold
import com.ramankumar.moviefinder.ui.compose.theme.Red
import com.ramankumar.moviefinder.ui.compose.theme.TextGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    movieTitle: String,
    movieYear: String,
    movieRating: Double,
    movieOverview: String,
    movieBackdrop: String?,
    initialIsFavorite: Boolean,
    trailers: List<Video> = emptyList(),
    isLoadingTrailers: Boolean = false,
    onBackClick: () -> Unit,
    onFavoriteToggle: (Boolean) -> Unit,
    onTrailerClick: (String, String) -> Unit
) {
    var isFavorite by remember { mutableStateOf(initialIsFavorite) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(movieTitle, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = DarkBackground
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = movieTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = movieYear,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray
                    )

                    Text(
                        text = "⭐ %.1f/10".format(java.util.Locale.US, movieRating),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

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

                Text(
                    text = "Overview",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = movieOverview,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFCCCCCC)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Trailers",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoadingTrailers) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Red)
                    }
                } else if (trailers.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        trailers.take(5).forEach { trailer ->
                            TrailerItem(
                                trailer = trailer,
                                onClick = { onTrailerClick(trailer.key, trailer.name) }
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No trailers available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray
                    )
                }
            }
        }
    }
}