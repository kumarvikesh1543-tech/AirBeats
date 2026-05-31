package com.darkxvenom.airbeats.ui.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.darkxvenom.airbeats.R

@Composable
fun NeonPlaybackCore(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    shuffleModeEnabled: Boolean,
    onShuffle: () -> Unit,
    repeatMode: Int,
    onRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "core_pulse_transition")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val intensity by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "intensity"
    )

    val playPauseInteractionSource = remember { MutableInteractionSource() }
    val isPlayPausePressed by playPauseInteractionSource.collectIsPressedAsState()
    val playPauseScale by animateFloatAsState(
        targetValue = if (isPlayPausePressed) 0.92f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "play_pause_scale"
    )

    Box(
        modifier = modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        // BLUE RING EFFECTS (drawn behind the core button)
        Box(
            modifier = Modifier
                .size(250.dp)
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    val center = androidx.compose.ui.geometry.Offset(w / 2f, h / 2f)
                    
                    // Layer 5: Soft atmospheric beam (Screen blend mode)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF003CFF).copy(alpha = 0.35f * intensity),
                                Color(0xFF245DFF).copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = 125.dp.toPx()
                        ),
                        center = center,
                        radius = 125.dp.toPx(),
                        blendMode = BlendMode.Screen
                    )

                    // Layer 2: Radial gradient blur ring
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF245DFF).copy(alpha = 0.7f * intensity),
                                Color(0xFF5B84FF).copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = 90.dp.toPx()
                        ),
                        center = center,
                        radius = 90.dp.toPx(),
                        blendMode = BlendMode.Screen
                    )

                    // Layer 3: Animated pulsing ring
                    drawCircle(
                        color = Color(0xFFAFC6FF).copy(alpha = pulseAlpha * 0.4f),
                        radius = 90.dp.toPx() * pulseScale,
                        center = center,
                        style = Stroke(width = 2.dp.toPx()),
                        blendMode = BlendMode.Screen
                    )

                    // Layer 4: Inner electric glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00C2FF).copy(alpha = 0.8f * intensity),
                                Color(0xFF003CFF).copy(alpha = 0.4f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = 60.dp.toPx()
                        ),
                        center = center,
                        radius = 60.dp.toPx(),
                        blendMode = BlendMode.Screen
                    )

                    // Layer 1: Solid blue circle backing
                    drawCircle(
                        color = Color(0xFF003CFF).copy(alpha = 0.15f),
                        radius = 54.dp.toPx(),
                        center = center
                    )
                }
        )

        // Center White Play/Pause Button
        Box(
            modifier = Modifier
                .size(74.dp)
                .scale(playPauseScale)
                .drawBehind {
                    // Strong blue outer shadow glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF003CFF).copy(alpha = 0.85f),
                                Color(0xFF245DFF).copy(alpha = 0.4f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = 56.dp.toPx()
                        ),
                        center = center,
                        radius = 56.dp.toPx()
                    )
                }
                .clip(CircleShape)
                .background(Color.White)
                .clickable(
                    interactionSource = playPauseInteractionSource,
                    indication = null,
                    onClick = onPlayPause
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(36.dp)
            )
        }

        // Surrounding Controls (Glassmorphic Circles)
        
        // Previous Button (Left)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 10.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.14f))
                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                .clickable(onClick = onPrevious),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.skip_previous),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        // Next Button (Right)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-10).dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.14f))
                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                .clickable(onClick = onNext),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.skip_next),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        // Shuffle Button (Top Left)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 34.dp, y = 28.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.14f))
                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                .clickable(onClick = onShuffle),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.shuffle),
                contentDescription = null,
                tint = if (shuffleModeEnabled) Color(0xFF00E5FF) else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Repeat Button (Top Right)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-34).dp, y = 28.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.14f))
                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                .clickable(onClick = onRepeat),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    if (repeatMode == androidx.media3.common.Player.REPEAT_MODE_ONE) R.drawable.repeat_one else R.drawable.repeat
                ),
                contentDescription = null,
                tint = if (repeatMode != androidx.media3.common.Player.REPEAT_MODE_OFF) Color(0xFF00E5FF) else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
