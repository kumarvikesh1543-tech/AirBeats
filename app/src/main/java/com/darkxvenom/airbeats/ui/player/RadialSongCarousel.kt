package com.darkxvenom.airbeats.ui.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Timeline
import com.darkxvenom.airbeats.extensions.metadata
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RadialSongCarousel(
    queueWindows: List<Timeline.Window>,
    currentWindowIndex: Int,
    onSongSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val density = LocalDensity.current
    
    val itemWidthDp = 180.dp
    val itemWidthPx = with(density) { itemWidthDp.toPx() }
    
    var parentWidthPx by remember { mutableStateOf(0f) }
    
    LaunchedEffect(currentWindowIndex, parentWidthPx) {
        if (currentWindowIndex in queueWindows.indices && parentWidthPx > 0f) {
            val itemCenterOffset = (parentWidthPx / 2f) - (itemWidthPx / 2f)
            listState.scrollToItem(currentWindowIndex, -itemCenterOffset.toInt())
        }
    }
    
    val activeIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) currentWindowIndex
            else {
                val center = layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2f
                var closestIndex = currentWindowIndex
                var minDistance = Float.MAX_VALUE
                for (itemInfo in visibleItems) {
                    val itemCenter = itemInfo.offset + itemInfo.size / 2f
                    val distance = kotlin.math.abs(itemCenter - center)
                    if (distance < minDistance) {
                        minDistance = distance
                        closestIndex = itemInfo.index
                    }
                }
                closestIndex
            }
        }
    }
    
    LaunchedEffect(activeIndex) {
        if (activeIndex in queueWindows.indices && activeIndex != currentWindowIndex) {
            onSongSelected(activeIndex)
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .onGloballyPositioned { coordinates ->
                parentWidthPx = coordinates.size.width.toFloat()
            },
        contentAlignment = Alignment.Center
    ) {
        if (parentWidthPx > 0f) {
            val centerScreenPx = parentWidthPx / 2f
            val contentPadding = PaddingValues(
                start = with(density) { (centerScreenPx - itemWidthPx / 2f).toDp() },
                end = with(density) { (centerScreenPx - itemWidthPx / 2f).toDp() }
            )

            LazyRow(
                state = listState,
                flingBehavior = flingBehavior,
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(64.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    items = queueWindows,
                    key = { _, item -> item.uid.hashCode() }
                ) { index, window ->
                    val metadata = window.mediaItem.metadata!!
                    
                    val distanceFraction = remember(listState) {
                        derivedStateOf {
                            val layoutInfo = listState.layoutInfo
                            val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                            if (itemInfo != null) {
                                val itemCenter = itemInfo.offset + itemInfo.size / 2f
                                val distance = itemCenter - centerScreenPx
                                (distance / centerScreenPx).coerceIn(-1f, 1f)
                            } else {
                                if (index < listState.firstVisibleItemIndex) -1f else 1f
                            }
                        }
                    }
                    
                    val radiusPx = with(density) { 260.dp.toPx() }
                    val rotationAngle = distanceFraction.value * 30f // Left tilts -30, right tilts +30
                    val angleRad = (rotationAngle * PI / 180f)
                    
                    val transY = radiusPx * (1f - cos(angleRad).toFloat())
                    val transX = radiusPx * sin(angleRad).toFloat() - (distanceFraction.value * with(density) { 12.dp.toPx() })

                    val isSelected = index == activeIndex
                    val alpha = if (isSelected) 1f else (1f - kotlin.math.abs(distanceFraction.value) * 0.7f).coerceIn(0.2f, 0.4f)
                    val scale = if (isSelected) 1f else (1f - kotlin.math.abs(distanceFraction.value) * 0.2f).coerceIn(0.8f, 0.9f)
                    val fontSize = if (isSelected) 18.sp else 13.sp

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(itemWidthDp)
                            .graphicsLayer {
                                rotationZ = rotationAngle
                                translationY = transY + 8.dp.toPx()
                                translationX = transX
                                this.alpha = alpha
                                scaleX = scale
                                scaleY = scale
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = String.format("%02d", index + 1),
                                color = if (isSelected) Color.White.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.35f),
                                fontSize = if (isSelected) 12.sp else 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                fontFamily = SpotifyFontFamily
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = metadata.title,
                                color = Color.White,
                                fontSize = fontSize,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                fontFamily = SpotifyFontFamily
                            )
                        }
                        
                        Spacer(Modifier.height(2.dp))
                        
                        Text(
                            text = metadata.artists.joinToString { it.name }.uppercase(),
                            color = if (isSelected) Color.White.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.25f),
                            fontSize = if (isSelected) 11.sp else 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            fontFamily = SpotifyFontFamily
                        )
                    }
                }
            }
        }
    }
}
