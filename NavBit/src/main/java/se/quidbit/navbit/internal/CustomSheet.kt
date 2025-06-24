package se.quidbit.navbit.internal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun CustomSheet(
    locked : Boolean,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    content: @Composable () -> Unit
) {
    val sheetAnimationSpec: AnimationSpec<Float> = tween(
        durationMillis = OVERLAY_ANIMATION_MS,
        easing = {f -> f*f}
    )

    val density = LocalDensity.current
    val dragThresholdPx = with(density) { 64.dp.toPx() }

    val coroutineScope = rememberCoroutineScope()

    var isDragging by remember { mutableStateOf(false) }
    var rawOffset by remember { mutableFloatStateOf(0f) }

    val animatedOffset = remember { Animatable(0f) }

    val offsetToUse = if (isDragging) rawOffset else animatedOffset.value

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = modifier
                .widthIn(max = 600.dp)
                .wrapContentHeight()
                .offset { IntOffset(0, offsetToUse.roundToInt()) }
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .then(
                    if (locked) {
                        Modifier
                    } else {
                        Modifier.pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragStart = {
                                    isDragging = true
                                },
                                onVerticalDrag = { change, delta ->
                                    change.consume()
                                    rawOffset = (rawOffset + delta).coerceAtLeast(0f)

                                    coroutineScope.launch {
                                        animatedOffset.snapTo(rawOffset)
                                    }
                                },
                                onDragEnd = {
                                    isDragging = false

                                    coroutineScope.launch {
                                        // Snap to current raw offset before animating
                                        animatedOffset.snapTo(rawOffset)

                                        if (rawOffset > dragThresholdPx) {
                                            onClose()
                                        } else {
                                            animatedOffset.animateTo(
                                                0f,
                                                animationSpec = sheetAnimationSpec
                                            )
                                            rawOffset = 0f
                                        }
                                    }
                                },
                                onDragCancel = {
                                    isDragging = false
                                    coroutineScope.launch {
                                        animatedOffset.animateTo(0f, animationSpec = sheetAnimationSpec)
                                        rawOffset = 0f
                                    }
                                }
                            )
                        }
                    }
                ),
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            content()
        }
    }
}

@Composable
fun CustomPopup(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = modifier
                .widthIn(max = 600.dp)
                .padding(64.dp)
                .clip(RoundedCornerShape(16.dp)),
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            content()
        }
    }
}