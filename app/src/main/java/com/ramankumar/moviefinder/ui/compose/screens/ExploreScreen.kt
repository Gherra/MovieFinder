package com.ramankumar.moviefinder.ui.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ExploreScreen(
    movies: List<Movie>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    onMovieClick: (Movie) -> Unit,
    onFavoriteClick: (Movie) -> Unit,
    onFilterChanged: (Int) -> Unit,
    onRefresh: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableIntStateOf(0) }

    // Separate scroll states for each filter!
    val trendingScrollState = rememberLazyGridState()
    val topRatedScrollState = rememberLazyGridState()
    val recentScrollState = rememberLazyGridState()

    // Determine which scroll state to use based on selected filter
    val currentScrollState = when (selectedFilter) {
        0 -> trendingScrollState
        1 -> topRatedScrollState
        2 -> recentScrollState
        else -> trendingScrollState
    }

    // Pull-to-refresh state
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { onRefresh(selectedFilter) }
    )

    // Load data when filter changes
    LaunchedEffect(selectedFilter) {
        onFilterChanged(selectedFilter)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // Title Section
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                    onFilterChanged(0)
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
                    onFilterChanged(1)
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
                    onFilterChanged(2)
                },
                label = { Text("\uD83C\uDF7F In Theatres") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Red,
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF1F1F1F),
                    labelColor = Color(0xFF888888)
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Movies Grid with Pull-to-Refresh
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            when {
                isLoading && movies.isEmpty() -> LoadingIndicator()
                movies.isEmpty() -> EmptyState(
                    emoji = "🎬",
                    title = "No Movies Found",
                    message = "Pull to refresh or check your connection"
                )
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = currentScrollState,
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(movies) { movie ->
                        MovieCard(
                            movie = movie,
                            onMovieClick = { onMovieClick(movie) },
                            onFavoriteClick = { onFavoriteClick(movie) }
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = Color(0xFF2C2C2C),
                contentColor = Red
            )
        }
    }
}
