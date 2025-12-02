package com.ramankumar.moviefinder.ui.compose.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ramankumar.moviefinder.model.auth.AuthResult
import com.ramankumar.moviefinder.ui.auth.AuthViewModel
import com.ramankumar.moviefinder.ui.compose.theme.DarkBackground
import com.ramankumar.moviefinder.ui.compose.theme.DarkSurface
import com.ramankumar.moviefinder.ui.compose.theme.Red
import com.ramankumar.moviefinder.ui.compose.theme.TextGray

@Composable
fun LoginScreen(vm: AuthViewModel) {
    val authState = vm.authState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }

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
            Text(
                text = if (isRegisterMode) "Create Account" else "Welcome to MovieFinder",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = TextGray) },
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

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = TextGray) },
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
                            color = Red,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }
                is AuthResult.Success -> {
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
                Text(if (isRegisterMode) "Register" else "Login")
            }

            TextButton(
                onClick = { isRegisterMode = !isRegisterMode },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Red
                )
            ) {
                Text(
                    text = if (isRegisterMode) "Already have an account? Login" else "Don't have an account? Register"
                )
            }
        }
    }
}