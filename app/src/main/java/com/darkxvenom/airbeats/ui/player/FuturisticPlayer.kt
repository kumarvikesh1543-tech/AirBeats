package com.darkxvenom.airbeats.ui.player

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.darkxvenom.airbeats.LocalPlayerConnection
import com.darkxvenom.airbeats.R
import com.darkxvenom.airbeats.ui.utils.highQualityThumbnail
import com.darkxvenom.airbeats.models.MediaMetadata
import com.darkxvenom.airbeats.utils.makeTimeString

@Composable
fun FuturisticPlayer(
    mediaMetadata: MediaMetadata?,
    position: Long,
    duration: Long,
    isPlaying: Boolean,
    isLoading: Boolean,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    repeatMode: Int,
    shuffleModeEnabled: Boolean,
    onSeek: (Long) -> Unit,
    onSeekFinished: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenQueue: () -> Unit,
    onCollapse: () -> Unit,
    onOpenMenu: () -> Unit,
    dynamicColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val playerConnection = LocalPlayerConnection.current ?: return
    
    val queueWindows by playerConnection.queueWindows.collectAsState(initial = emptyList())
    val currentWindowIndex = playerConnection.player.currentMediaItemIndex

    // Custom gesture detector for swipe down to collapse player
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    if (dragAmount.y > 18f && kotlin.math.abs(dragAmount.x) < 12f) {
                        onCollapse()
                    }
                }
            }
    ) {
        // Atmospheric Ambient Background Glow
        AtmosphericBackground(
            dynamicColor = dynamicColor,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onCollapse) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = "Collapse Player",
                        tint = Color.White
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "AIRBEATS FUTURISTIC",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = SpotifyFontFamily
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = mediaMetadata?.album?.title ?: "SINGLE",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = SpotifyFontFamily
                    )
                }

                IconButton(onClick = onOpenMenu) {
                    Icon(
                        painter = painterResource(R.drawable.more_vert),
                        contentDescription = "More Options",
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // MONOCHROME CINEMATIC ALBUM ART SECTION
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .fillMaxHeight(0.42f)
                    .clip(RoundedCornerShape(32.dp))
            ) {
                // Monochrome cinematic desaturated image
                val colorMatrix = remember {
                    ColorMatrix().apply { setToSaturation(0.12f) }
                }
                
                AsyncImage(
                    model = mediaMetadata?.thumbnailUrl?.highQualityThumbnail(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.colorMatrix(colorMatrix),
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = 0.92f }
                )

                // Fading atmospheric vertical gradient overlay (transparent to black at the bottom)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF050505).copy(alpha = 0.85f),
                                    Color(0xFF050505)
                                )
                            )
                        )
                )
            }

            Spacer(Modifier.weight(1f))

            // CURVED SONG CAROUSEL
            RadialSongCarousel(
                queueWindows = queueWindows,
                currentWindowIndex = currentWindowIndex,
                onSongSelected = { index ->
                    playerConnection.player.seekToDefaultPosition(index)
                    playerConnection.player.playWhenReady = true
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(18.dp))

            // ARC PROGRESS & CORE WHEEL SECTION
            Box(
                modifier = Modifier
                    .size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                // Curved Progress Arc
                ArcProgressBar(
                    position = position,
                    duration = duration,
                    onSeek = onSeek,
                    onSeekFinished = onSeekFinished,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Neon Playback Core containing play controls inside the arc
                NeonPlaybackCore(
                    isPlaying = isPlaying,
                    onPlayPause = onPlayPause,
                    onPrevious = onPrevious,
                    onNext = onNext,
                    shuffleModeEnabled = shuffleModeEnabled,
                    onShuffle = onShuffle,
                    repeatMode = repeatMode,
                    onRepeat = onRepeat,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Heart & Time section stacked in the upper-middle area of the dial
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 110.dp) // Offset above the play button
                ) {
                    val isLiked = playerConnection.currentSong.collectAsState(initial = null).value?.song?.liked == true
                    
                    IconButton(
                        onClick = playerConnection::toggleLike,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(if (isLiked) R.drawable.favorite else R.drawable.favorite_border),
                            contentDescription = "Like Song",
                            tint = if (isLiked) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "${makeTimeString(position)} / ${makeTimeString(duration)}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 1.sp,
                        fontFamily = SpotifyFontFamily
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Dynamic bottom action controls (Lyrics, Queue)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onOpenLyrics) {
                    Icon(
                        painter = painterResource(R.drawable.lyrics),
                        contentDescription = "Open Lyrics",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(onClick = onOpenQueue) {
                    Icon(
                        painter = painterResource(R.drawable.queue_music),
                        contentDescription = "Open Queue",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
