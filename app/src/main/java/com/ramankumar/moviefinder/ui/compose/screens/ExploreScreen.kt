package com.ramankumar.moviefinder.ui.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramankumar.moviefinder.model.Movie
import com.ramankumar.moviefinder.ui.compose.components.EmptyState
import com.ramankumar.moviefinder.ui.compose.components.LoadingIndicator
import com.ramankumar.moviefinder.ui.compose.components.MovieCard
import com.ramankumar.moviefinder.ui.compose.theme.Red

@Composable
fun ExploreScreen(
    movies: List<Movie>,
    isLoading: Boolean,
    onMovieClick: (Movie) -> Unit,
    onFavoriteClick: (Movie) -> Unit,
    onFilterChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableIntStateOf(0) }  // ← FIXED: mutableIntStateOf

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // Title Section
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Explore Movies",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Discover your next favorite film",
                fontSize = 14.sp,
                color = Color(0xFF888888)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == 0,
                onClick = {
                    selectedFilter = 0
                    onFilterChanged(0)  // ← CALL CALLBACK!
                },
                label = { Text("🔥 Trending") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Red,
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF1F1F1F),
                    labelColor = Color(0xFF888888)
                )
            )

            FilterChip(
                selected = selectedFilter == 1,
                onClick = {
                    selectedFilter = 1
                    onFilterChanged(1)  // ← CALL CALLBACK!
                },
                label = { Text("⭐ Top Rated") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Red,
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF1F1F1F),
                    labelColor = Color(0xFF888888)
                )
            )

            FilterChip(
                selected = selectedFilter == 2,
                onClick = {
                    selectedFilter = 2
                    onFilterChanged(2)  // ← CALL CALLBACK!
                },
                label = { Text("📅 Recent") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Red,
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF1F1F1F),
                    labelColor = Color(0xFF888888)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> LoadingIndicator()

                movies.isEmpty() -> EmptyState(
                    emoji = "🎬",
                    title = "No Movies Found",
                    message = "Try refreshing or check your connection"
                )

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(movies) { movie ->
                            // REMOVED AnimatedVisibility - simpler and works!
                            MovieCard(
                                movie = movie,
                                onMovieClick = { onMovieClick(movie) },
                                onFavoriteClick = { onFavoriteClick(movie) }
                            )
                        }
                    }
                }
            }
        }
    }
}