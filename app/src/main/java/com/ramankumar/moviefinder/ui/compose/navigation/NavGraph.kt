package com.ramankumar.moviefinder.ui.compose.navigation

sealed class Screen(val route: String) {
    object Explore : Screen("explore")
    object Search : Screen("search")
    object Favorites : Screen("favorites")
}