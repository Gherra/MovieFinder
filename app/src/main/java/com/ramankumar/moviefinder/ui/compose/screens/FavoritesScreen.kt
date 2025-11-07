package com.ramankumar.moviefinder.ui.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramankumar.moviefinder.model.Movie
import com.ramankumar.moviefinder.ui.compose.components.EmptyState
import com.ramankumar.moviefinder.ui.compose.components.MovieCard

@Composable
fun FavoritesScreen(
    favorites: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    onFavoriteClick: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // Title
        Text(
            text = "Your Favorites",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        if (favorites.isEmpty()) {
            EmptyState(
                emoji = "❤️",
                title = "No Favourites Yet",
                message = "Start swiping right on movies you love to build your collection"
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(favorites) { movie ->
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