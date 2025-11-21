package com.ramankumar.moviefinder.ui.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ramankumar.moviefinder.model.Movie
import com.ramankumar.moviefinder.ui.compose.theme.Gold
import com.ramankumar.moviefinder.ui.compose.theme.Red

@Composable
fun MovieCard(
    movie: Movie,
    onMovieClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onMovieClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F1F1F)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Movie Poster
            AsyncImage(
                model = movie.getPosterUrl(),
                contentDescription = movie.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentScale = ContentScale.Crop
            )

            // Movie Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Title
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp).padding(top = 8.dp),
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Year and Rating
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = movie.releaseDate.take(4),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF888888)
                    )

                    Text(
                        text = "⭐ ${String.format("%.1f", movie.voteAverage)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gold
                    )
                }


                // Favorite Button
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = if (movie.isFavorite)
                            Icons.Filled.Star
                        else
                            Icons.Outlined.StarOutline,

                        contentDescription = if (movie.isFavorite)
                            "Remove from favorites"
                        else
                            "Add to favorites",

                        tint = if (movie.isFavorite) Gold else Red
                    )
                }
            }
        }
    }
}
