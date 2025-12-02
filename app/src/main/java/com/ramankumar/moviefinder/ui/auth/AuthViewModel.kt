package com.ramankumar.moviefinder.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ramankumar.moviefinder.data.repository.auth.AuthRepository
import com.ramankumar.moviefinder.model.auth.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthResult>(
        if (repository.currentUser() != null) AuthResult.Success else AuthResult.Failure(null)
    )
    val authState = _authState.asStateFlow()

    // Observe Firebase auth state changes
    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        _authState.value = if (auth.currentUser != null) {
            AuthResult.Success
        } else {
            AuthResult.Failure(null)
        }
    }

    init {
        // Add auth state listener to observe login/logout
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            _authState.value = repository.sendPasswordReset(email)
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            _authState.value = repository.login(email, password)
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            _authState.value = repository.register(email, password)
        }
    }

    fun logout() {
        repository.logout()
    }

    override fun onCleared() {
        super.onCleared()
        // Remove auth state listener when ViewModel is cleared
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
    }
}