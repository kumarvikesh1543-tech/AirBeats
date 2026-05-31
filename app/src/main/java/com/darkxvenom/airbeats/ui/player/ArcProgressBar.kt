package com.darkxvenom.airbeats.ui.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun ArcProgressBar(
    position: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    onSeekFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = remember(position, duration) {
        if (duration > 0) (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
    }

    val startAngle = 140f
    val sweepAngle = 260f
    
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableStateOf(0f) }

    val activeProgress = if (isDragging) dragProgress else progress

    Box(
        modifier = modifier
            .size(280.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        isDragging = true
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        val center = Offset(w / 2f, h / 2f)
                        val fraction = calculateProgressFromOffset(offset, center)
                        dragProgress = fraction
                        onSeek((fraction * duration).toLong())
                        tryAwaitRelease()
                        isDragging = false
                        onSeekFinished()
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        val center = Offset(w / 2f, h / 2f)
                        val fraction = calculateProgressFromOffset(offset, center)
                        dragProgress = fraction
                        onSeek((fraction * duration).toLong())
                    },
                    onDrag = { change, _ ->
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        val center = Offset(w / 2f, h / 2f)
                        val fraction = calculateProgressFromOffset(change.position, center)
                        dragProgress = fraction
                        onSeek((fraction * duration).toLong())
                    },
                    onDragEnd = {
                        isDragging = false
                        onSeekFinished()
                    },
                    onDragCancel = {
                        isDragging = false
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h / 2f)
            val strokeWidth = 4.dp.toPx()
            val radius = (min(w, h) / 2f) - 16.dp.toPx()
            
            val arcSize = Size(radius * 2f, radius * 2f)
            val topLeft = Offset(center.x - radius, center.y - radius)

            // Draw inactive track (15% white)
            drawArc(
                color = Color.White.copy(alpha = 0.15f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Draw active progress (pure white)
            val activeSweep = sweepAngle * activeProgress
            drawArc(
                color = Color.White,
                startAngle = startAngle,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Calculate dot coords
            val dotAngleDeg = startAngle + activeSweep
            val dotAngleRad = Math.toRadians(dotAngleDeg.toDouble())
            val dotX = center.x + radius * cos(dotAngleRad).toFloat()
            val dotY = center.y + radius * sin(dotAngleRad).toFloat()
            val dotCenter = Offset(dotX, dotY)

            // Dot atmospheric glow
            val glowRadius = 14.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.8f),
                        Color(0xFF00E5FF).copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = dotCenter,
                    radius = glowRadius
                ),
                center = dotCenter,
                radius = glowRadius
            )

            // Dot core
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = dotCenter
            )
        }
    }
}

private fun calculateProgressFromOffset(offset: Offset, center: Offset): Float {
    val dx = offset.x - center.x
    val dy = offset.y - center.y
    var angleRad = atan2(dy, dx)
    var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
    
    if (angleDeg < 0) {
        angleDeg += 360f
    }

    var relativeAngle = angleDeg - 140f
    if (relativeAngle < 0f) {
        relativeAngle += 360f
    }

    val sweepAngle = 260f
    val fraction = if (relativeAngle > sweepAngle) {
        if (relativeAngle > sweepAngle + (360f - sweepAngle) / 2f) 0f else 1f
    } else {
        relativeAngle / sweepAngle
    }
    
    return fraction.coerceIn(0f, 1f)
}
