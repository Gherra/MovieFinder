package com.ramankumar.moviefinder.ui.splash

import android.content.Intent
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.ramankumar.moviefinder.ui.compose.components.MovieFinderLogo
import com.ramankumar.moviefinder.ui.compose.theme.DarkBackground
import com.ramankumar.moviefinder.ui.compose.theme.MovieFinderTheme
import com.ramankumar.moviefinder.ui.main.MainActivityCompose
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MovieFinderTheme {
                NetflixSplashScreen {
                    startActivity(Intent(this, MainActivityCompose::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
            }
        }
    }
}

@Composable
private fun NetflixSplashScreen(onFinished: () -> Unit) {
    val logoScale = remember { Animatable(0.65f) }
    val sweep = remember { Animatable(-0.4f) }
    val glow = remember { Animatable(0f) }
    val overshoot = remember { OvershootInterpolator(1.4f) }

    LaunchedEffect(Unit) {
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 950, easing = Easing { overshoot.getInterpolation(it) })
        )
        glow.animateTo(targetValue = 1f, animationSpec = tween(650))
        sweep.animateTo(targetValue = 1.2f, animationSpec = tween(850))
        delay(600)
        glow.animateTo(targetValue = 0f, animationSpec = tween(350))
        delay(150)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        MovieFinderLogo(
            modifier = Modifier
                .align(Alignment.Center)
                .size(260.dp)
                .graphicsLayer {
                    scaleX = logoScale.value
                    scaleY = logoScale.value
                },
            sweepProgress = sweep.value,
            glowAlpha = glow.value
        )

        Text(
            text = "MOVIEFINDER",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 6.sp,
            textAlign = TextAlign.Center
        )
    }
}
