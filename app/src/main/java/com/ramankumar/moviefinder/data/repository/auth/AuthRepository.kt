package com.ramankumar.moviefinder.data.repository.auth

import com.google.firebase.auth.FirebaseAuth
import com.ramankumar.moviefinder.model.auth.AuthResult
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    suspend fun login(email: String, password: String): AuthResult{
        return try{
            auth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success
        } catch (e: Exception){
            AuthResult.Failure(e.message)
        }
    }

    suspend fun register(email: String, password: String): AuthResult{
        return try {
            auth.createUserWithEmailAndPassword(email,password).await()
            AuthResult.Success
        } catch (e: Exception){
            AuthResult.Failure(e.message)
        }
    }

    suspend fun sendPasswordReset(email: String): AuthResult {
        return try {
            auth.sendPasswordResetEmail(email).await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Failure(e.message)
        }
    }

    fun logout(){
        auth.signOut()
    }

    fun currentUser() = auth.currentUser



}