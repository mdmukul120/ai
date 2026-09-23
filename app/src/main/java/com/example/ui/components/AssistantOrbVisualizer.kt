package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CharuCyan
import com.example.ui.theme.CharuIndigo
import com.example.ui.theme.CharuViolet

@Composable
fun AssistantOrbVisualizer(
    isListening: Boolean,
    isSpeaking: Boolean,
    isThinking: Boolean,
    audioDb: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    val baseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "base_scale"
    )

    val waveRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val dynamicPulse = if (isListening) {
        (audioDb * 0.08f).coerceIn(0f, 0.4f)
    } else if (isSpeaking) {
        0.15f
    } else if (isThinking) {
        0.25f
    } else {
        0f
    }

    val totalScale = (baseScale + dynamicPulse).coerceIn(0.7f, 1.6f)

    Box(
        modifier = modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2.6f) * totalScale

            // Outer ethereal glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CharuCyan.copy(alpha = if (isListening) 0.45f else 0.2f),
                        CharuIndigo.copy(alpha = if (isSpeaking) 0.35f else 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.5f
                ),
                center = center,
                radius = baseRadius * 1.5f
            )

            // Middle vibrant ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isListening) CharuCyan else CharuIndigo,
                        CharuViolet.copy(alpha = 0.6f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius
                ),
                center = center,
                radius = baseRadius
            )

            // Core nucleus
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White,
                        if (isListening) CharuCyan else CharuViolet
                    ),
                    start = Offset(center.x - baseRadius * 0.5f, center.y - baseRadius * 0.5f),
                    end = Offset(center.x + baseRadius * 0.5f, center.y + baseRadius * 0.5f)
                ),
                center = center,
                radius = baseRadius * 0.45f
            )
        }
    }
}
