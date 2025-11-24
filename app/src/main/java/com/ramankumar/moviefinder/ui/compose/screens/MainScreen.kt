package com.ramankumar.moviefinder.ui.compose.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramankumar.moviefinder.model.Movie
import com.ramankumar.moviefinder.ui.compose.navigation.Screen
import com.ramankumar.moviefinder.ui.compose.theme.Red
import com.ramankumar.moviefinder.ui.main.MainViewModel

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val label: String
) {
    object Swipe : BottomNavItem(
        "swipe",
        Icons.Outlined.Style,
        Icons.Filled.Style,
        "Swipe"
    )
    object Search : BottomNavItem(
        Screen.Search.route,
        Icons.Outlined.Search,
        Icons.Filled.Search,
        "Search"
    )
    object Explore : BottomNavItem(
        Screen.Explore.route,
        Icons.Outlined.Explore,
        Icons.Outlined.Explore,
        "Explore"
    )
    object ForYou : BottomNavItem(
        "for_you",
        Icons.Outlined.StarBorder,
        Icons.Filled.Star,
        "For You"
    )
    object Favorites : BottomNavItem(
        Screen.Favorites.route,
        Icons.Outlined.FavoriteBorder,
        Icons.Filled.Favorite,
        "Favourites"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onMovieClick: (Movie) -> Unit,
    onSwipeClick: () -> Unit
) {
    val movies by viewModel.movies.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val recommendations by viewModel.recommendations.collectAsStateWithLifecycle()
    val isFirstSearch by viewModel.isFirstSearch.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var selectedRoute by remember { mutableStateOf(Screen.Explore.route) }
    var searchQuery by remember { mutableStateOf("") }

    val bottomNavItems = listOf(
        BottomNavItem.Swipe,
        BottomNavItem.Search,
        BottomNavItem.Explore,
        BottomNavItem.ForYou,
        BottomNavItem.Favorites
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "MovieFinder",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1F1F1F)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1F1F1F),
                contentColor = Color.White
            ) {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (selectedRoute == item.route) item.selectedIcon else item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        selected = selectedRoute == item.route,
                        onClick = {
                            when (item.route) {
                                "swipe" -> onSwipeClick()
                                "for_you" -> {
                                    selectedRoute = item.route
                                    viewModel.loadRecommendations()
                                }
                                else -> {
                                    selectedRoute = item.route
                                    viewModel.setCurrentTab(
                                        when (item.route) {
                                            Screen.Explore.route -> 0
                                            Screen.Search.route -> 1
                                            Screen.Favorites.route -> 2
                                            else -> 0
                                        }
                                    )
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Red,
                            selectedTextColor = Red,
                            unselectedIconColor = Color(0xFF888888),
                            unselectedTextColor = Color(0xFF888888),
                            indicatorColor = Color(0xFF2C2C2C)
                        )
                    )
                }
            }
        },
        containerColor = Color.Black
    ) { paddingValues ->
        when (selectedRoute) {
            Screen.Explore.route -> {
                ExploreScreen(
                    movies = movies,
                    isLoading = isLoading,
                    isRefreshing = isRefreshing,
                    onMovieClick = onMovieClick,
                    onFavoriteClick = { movie ->
                        viewModel.toggleFavorite(movie.id)
                    },
                    onFilterChanged = { filterIndex ->
                        when (filterIndex) {
                            0 -> viewModel.loadTrendingMovies() // Trending
                            1 -> viewModel.loadTopRatedMovies() // Top Rated
                            2 -> viewModel.loadNowPlayingMovies() // Recent
                        }
                    },
                    onRefresh = { filterIndex ->  // ← RECEIVE FILTER INDEX
                        when (filterIndex) {
                            0 -> viewModel.loadTrendingMovies(refresh = true)
                            1 -> viewModel.loadTopRatedMovies(refresh = true)
                            2 -> viewModel.loadNowPlayingMovies(refresh = true)
                        }
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            Screen.Search.route -> {
                SearchScreen(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onSearch = {
                        if (searchQuery.isNotEmpty()) {
                            viewModel.searchMovies(searchQuery)
                        }
                    },
                    movies = movies,
                    isFirstSearch = isFirstSearch,
                    isLoading = isLoading,
                    onMovieClick = onMovieClick,
                    onFavoriteClick = { movie ->
                        viewModel.toggleFavorite(movie.id)
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            "for_you" -> {
                RecommendationsScreen(
                    movies = recommendations,
                    isLoading = isLoading,
                    isRefreshing = isRefreshing, // <-- Added
                    error = error,
                    onMovieClick = onMovieClick,
                    onFavoriteClick = { movie ->
                        viewModel.toggleFavorite(movie.id)
                    },
                    onRefresh = {
                        viewModel.loadRecommendations()
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            Screen.Favorites.route -> {
                FavoritesScreen(
                    favorites = favorites,
                    onMovieClick = onMovieClick,
                    onFavoriteClick = { movie ->
                        viewModel.toggleFavorite(movie.id)
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}