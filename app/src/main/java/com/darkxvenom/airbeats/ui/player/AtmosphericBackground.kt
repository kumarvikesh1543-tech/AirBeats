package com.darkxvenom.airbeats.ui.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AtmosphericBackground(
    modifier: Modifier = Modifier,
    dynamicColor: Color? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow_transition")
    
    // Scale animation
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )
    
    // Alpha animation
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Beam offset animation for moving soft light fog / beam
    val beamOffset by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beam_offset"
    )

    val primaryColor = dynamicColor ?: Color(0xFF0047FF)
    val secondaryColor = Color(0xFF1A4DFF)
    val tertiaryColor = Color(0xFF3A6DFF)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF151515),
                        Color(0xFF090909),
                        Color(0xFF000000)
                    )
                )
            )
    ) {
        // Glowing overlay behind elements
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(120.dp)
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    
                    // Base giant radial glow centered near lower-middle area (h * 0.78f)
                    val centerGlow = Offset(w / 2f, h * 0.78f)
                    val dynamicRadius = w * 1.1f * scale

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = alpha),
                                secondaryColor.copy(alpha = alpha * 0.6f),
                                tertiaryColor.copy(alpha = alpha * 0.2f),
                                Color.Transparent
                            ),
                            center = centerGlow,
                            radius = dynamicRadius
                        ),
                        center = centerGlow,
                        radius = dynamicRadius
                    )

                    // Moving soft light fog / beam overlay
                    val beamCenter = Offset(w / 2f + beamOffset, h * 0.72f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00C2FF).copy(alpha = alpha * 0.35f),
                                Color(0xFF0047FF).copy(alpha = alpha * 0.1f),
                                Color.Transparent
                            ),
                            center = beamCenter,
                            radius = w * 0.65f * scale
                        ),
                        center = beamCenter,
                        radius = w * 0.65f * scale
                    )
                }
        )
    }
}
