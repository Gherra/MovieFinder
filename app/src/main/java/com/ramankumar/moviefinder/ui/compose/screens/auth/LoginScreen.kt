package com.ramankumar.moviefinder.ui.compose.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ramankumar.moviefinder.R
import com.ramankumar.moviefinder.model.auth.AuthResult
import com.ramankumar.moviefinder.ui.auth.AuthViewModel
import com.ramankumar.moviefinder.ui.compose.theme.DarkBackground
import com.ramankumar.moviefinder.ui.compose.theme.DarkSurface
import com.ramankumar.moviefinder.ui.compose.theme.Red
import com.ramankumar.moviefinder.ui.compose.theme.TextGray

private val JetBrainsMono = FontFamily(
    Font(R.font.jetbrainsmono_regular, FontWeight.Normal),
    Font(R.font.jetbrainsmono_medium, FontWeight.Medium),
    Font(R.font.jetbrainsmono_bold, FontWeight.Bold),
    Font(R.font.jetbrainsmono_italic, FontWeight.Normal)
)

@Composable
fun LoginScreen(vm: AuthViewModel) {
    val authState = vm.authState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    var resetMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                text = if (isRegisterMode) "Create Account" else "Welcome to MovieFinder",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = {
                    Text(
                        "Email",
                        color = TextGray,
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Medium
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Red,
                    unfocusedBorderColor = TextGray,
                    focusedLabelColor = Red,
                    unfocusedLabelColor = TextGray
                )
            )

            // Password reset feedback
            resetMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextGray,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 8.dp)
                )
            }

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = {
                    Text(
                        "Password",
                        color = TextGray,
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Medium
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Red,
                    unfocusedBorderColor = TextGray,
                    focusedLabelColor = Red,
                    unfocusedLabelColor = TextGray
                )
            )

            // Show loading or error state
            when (val state = authState.value) {
                is AuthResult.Loading -> {
                    CircularProgressIndicator(
                        color = Red,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                is AuthResult.Failure -> {
                    if (state.message != null) {
                        Text(
                            text = "Error: ${state.message}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Red,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }
                is AuthResult.Success -> {
                    // If this was triggered by a resetPassword call 
                    if (!isRegisterMode && password.isBlank() && email.isNotBlank()) {
                        resetMessage = "If an account exists for $email, a reset link has been sent."
                    }
                }
            }

            Button(
                onClick = {
                    if (isRegisterMode) {
                        vm.register(email, password)
                    } else {
                        vm.login(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                enabled = email.isNotBlank() && password.isNotBlank() && authState.value !is AuthResult.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Red,
                    contentColor = Color.White,
                    disabledContainerColor = DarkSurface,
                    disabledContentColor = TextGray
                )
            ) {
                Text(
                    text = if (isRegisterMode) "Register" else "Login",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            TextButton(
                onClick = { isRegisterMode = !isRegisterMode },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Red
                )
            ) {
                Text(
                    text = if (isRegisterMode) "Already have an account? Login" else "Don't have an account? Register",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            // Forgot password
            if (!isRegisterMode) {
                TextButton(
                    onClick = {
                        if (email.isNotBlank()) {
                            resetMessage = "Sending password reset email..."
                            vm.resetPassword(email.trim())
                        } else {
                            resetMessage = "Enter your email first to reset your password."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = TextGray
                    )
                ) {
                    Text(
                        text = "Forgot password?",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}