package com.ramankumar.moviefinder.ui.compose.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicText
import kotlin.math.max
import kotlin.math.min

/** Netflix-inspired MovieFinder emblem with controllable sweep / glow parameters. */
@Composable
fun MovieFinderLogo(
    modifier: Modifier = Modifier,
    sweepProgress: Float = 0f,
    glowAlpha: Float = 1f
) {
    val ribbonBrush = remember {
        Brush.verticalGradient(listOf(Color(0xFFE50914), Color(0xFFA00000)))
    }
    val sweepBrush = remember {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.85f), Color.Transparent)
        )
    }

    Box(
        modifier = modifier
            .sizeIn(minWidth = 220.dp, minHeight = 220.dp)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        BasicText(
            text = "M",
            modifier = Modifier.fillMaxSize(),
            style = TextStyle(
                brush = ribbonBrush,
                fontSize = 200.sp,
                fontWeight = FontWeight.Black,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.65f),
                    offset = Offset(x = 0f, y = 16f),
                    blurRadius = 28f
                ),
                textAlign = TextAlign.Center,
                letterSpacing = (-8).sp
            )
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val sizeMin = min(size.width, size.height)
            val circleRadius = sizeMin * 0.22f
            val circleCenter = Offset(x = size.width * 0.5f, y = size.height * 0.55f)
            val strokeWidth = sizeMin * 0.03f

            drawCircle(
                color = Color(0xFF050505),
                radius = circleRadius + strokeWidth * 1.3f,
                center = circleCenter,
                alpha = 0.95f
            )
            drawCircle(
                color = Color(0xFFFFD700),
                radius = circleRadius,
                center = circleCenter,
                style = Stroke(width = strokeWidth)
            )

            val handleLength = circleRadius * 1.5f
            val handleWidth = strokeWidth * 1.2f
            drawLine(
                color = Color(0xFFFFD700),
                start = circleCenter + Offset(circleRadius * 0.7f, circleRadius * 0.7f),
                end = circleCenter + Offset(circleRadius * 0.7f + handleLength, circleRadius * 0.7f + handleLength),
                strokeWidth = handleWidth,
                cap = StrokeCap.Round
            )

            val clapboardWidth = circleRadius * 1.2f
            val clapboardHeight = circleRadius * 0.75f
            val clapboardTopLeft = circleCenter - Offset(clapboardWidth / 2f, clapboardHeight / 2f)
            drawRoundRect(
                color = Color(0xFFE50914),
                topLeft = clapboardTopLeft,
                size = Size(clapboardWidth, clapboardHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(strokeWidth)
            )

            val tickWidth = clapboardWidth / 4f
            val tickHeight = clapboardHeight / 3.5f
            for (i in 0 until 3) {
                val offsetX = clapboardTopLeft.x + i * (tickWidth * 0.9f) + strokeWidth
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.8f),
                    topLeft = Offset(offsetX, clapboardTopLeft.y + strokeWidth * 0.6f),
                    size = Size(tickWidth * 0.7f, tickHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(strokeWidth * 0.4f)
                )
            }

            val holeSize = strokeWidth * 0.65f
            val holeSpacing = holeSize * 1.8f
            for (j in 0 until 3) {
                val offsetX = clapboardTopLeft.x + clapboardWidth - (holeSpacing * (j + 1.3f))
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.85f),
                    topLeft = Offset(offsetX, clapboardTopLeft.y + clapboardHeight - holeSpacing * 1.4f),
                    size = Size(holeSize, holeSize),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(holeSize / 4f)
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(glowAlpha)
        ) {
            val clamped = max(-0.2f, min(1.2f, sweepProgress))
            val sweepWidth = size.width * 0.35f
            val startX = clamped * size.width - sweepWidth / 2f
            drawRect(
                brush = sweepBrush,
                topLeft = Offset(startX, -size.height * 0.2f),
                size = Size(sweepWidth, size.height * 1.4f)
            )
        }
    }
}
