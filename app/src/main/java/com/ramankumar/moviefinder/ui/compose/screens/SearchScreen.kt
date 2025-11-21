package com.ramankumar.moviefinder.ui.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ramankumar.moviefinder.model.Movie
import com.ramankumar.moviefinder.ui.compose.components.EmptyState
import com.ramankumar.moviefinder.ui.compose.components.LoadingIndicator
import com.ramankumar.moviefinder.ui.compose.components.MovieCard
import com.ramankumar.moviefinder.ui.compose.theme.Red

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    movies: List<Movie>,
    isFirstSearch: Boolean,
    isLoading: Boolean,
    onMovieClick: (Movie) -> Unit,
    onFavoriteClick: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search movies...") },
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = Red
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Clear",
                            tint = Color(0xFF888888)
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                // container color now per state
                focusedContainerColor = Color(0xFF1F1F1F),
                unfocusedContainerColor = Color(0xFF1F1F1F),
                disabledContainerColor = Color(0xFF1F1F1F),
                errorContainerColor = Color(0xFF1F1F1F),

                // text & cursor
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Red,

                // indicators (underline on TextField)
                focusedIndicatorColor = Red,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() })
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Results
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> LoadingIndicator()

                searchQuery.isEmpty() && isFirstSearch -> EmptyState(
                    emoji = "🔍",
                    title = "Search for Movies",
                    message = "Enter a movie title to start searching"
                )


                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(vertical = 8.dp),
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
            }
        }
    }
}