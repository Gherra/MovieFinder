package com.ramankumar.moviefinder.model.auth


/*
Used to determine Authenticator result acting similar to a Data class
 */
sealed class AuthResult {
    object Success : AuthResult()
    data class Failure(val message: String?) : AuthResult()
    object Loading : AuthResult()
}