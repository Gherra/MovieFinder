package com.ramankumar.moviefinder.ui.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
    onBackClick: () -> Unit,
    onFavoriteToggle: (Boolean) -> Unit
) {
    var isFavorite by remember { mutableStateOf(initialIsFavorite) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movie Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, "Back")
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
            // Backdrop Image
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
                        text = "⭐ ${String.format("%.1f", movieRating)}/10",
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
            }
        }
    }
}