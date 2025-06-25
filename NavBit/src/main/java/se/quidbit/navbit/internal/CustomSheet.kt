package se.quidbit.navbit.internal

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.exponentialDecay
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.ln
import kotlin.math.roundToInt

@Composable
fun CustomSheet(
    locked : Boolean,
    maxWidth : Dp,
    closingTime : MutableState<Int?>,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    content: @Composable () -> Unit
) {
    val sheetAnimationSpec: AnimationSpec<Float> = tween(
        durationMillis = OVERLAY_ANIMATION_MS,
        easing = {f -> f*f}
    )

    val coroutineScope = rememberCoroutineScope()

    var isDragging by remember { mutableStateOf(false) }
    var rawOffset by remember { mutableFloatStateOf(0f) }

    val animatedOffset = remember { Animatable(0f) }

    val offsetToUse = if (isDragging) rawOffset else animatedOffset.value

    val sheetHeightPx = remember { mutableIntStateOf(0) }
    val sheetDragThreshold = 0.5f
    val velocityThreshold = 3000f

    // ----------------------------------------------------------
    // To handle nested scroll views
    // ----------------------------------------------------------
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (locked) return Offset.Zero

                val deltaY = available.y

                if (deltaY < 0 && rawOffset > 0f) {
                    isDragging = true

                    // Compute how much we can consume
                    val newOffset = (rawOffset + deltaY).coerceAtLeast(0f)
                    val consumedY = rawOffset - newOffset
                    rawOffset = newOffset

                    coroutineScope.launch {
                        animatedOffset.snapTo(rawOffset)
                    }

                    return Offset(0f, -consumedY) // Return the negative value we handled to make sure the nested scroll stands still
                }

                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (locked) return Offset.Zero

                if ((available.y > 0 || isDragging) && consumed.y == 0f) {
                    isDragging = true
                    rawOffset = (rawOffset + available.y).coerceAtLeast(0f)
                    coroutineScope.launch {
                        animatedOffset.snapTo(rawOffset)
                    }
                    return Offset(0f, available.y)
                }

                return Offset.Zero
            }

            // ---------------------------------------------------------------------

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (locked) return Velocity.Zero

                // preFling is effectively when the finger is lifter
                isDragging = false

                animatedOffset.snapTo(rawOffset)

                // Collapse if dragged enough distance in total
                if (rawOffset > sheetHeightPx.intValue * sheetDragThreshold) {
                    closingTime.value = OVERLAY_ANIMATION_MS
                    onClose()
                    coroutineScope.launch {
                        animatedOffset.animateTo(sheetHeightPx.intValue.toFloat(), animationSpec = sheetAnimationSpec)
                    }
                } else {
                    rawOffset = 0f
                    coroutineScope.launch {
                        animatedOffset.animateTo(0f, animationSpec = sheetAnimationSpec)
                    }
                }

                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (locked) return Velocity.Zero

                coroutineScope.launch {
                    // Collapse if fast enough
                    // And if the children consumed almost nothing (so we don't close the sheet after a long list scroll reaches its end)
                    if (closingTime.value == null && available.y > velocityThreshold && consumed.y < 100f) {
                        val sheetHeight = sheetHeightPx.intValue.toFloat()

                        onClose()

                        // Try to use a natural decay (if the velocity is high enough for the sheet to leave)
                        val decay = exponentialDecay<Float>()
                        val target = decay.calculateTargetValue(rawOffset, available.y)

                        if (target >= sheetHeight) {
                            // Safe to decay — it'll go off screen
                            closingTime.value = estimateDecayTimeToTarget(rawOffset, available.y, sheetHeight)
                            animatedOffset.animateDecay(available.y, decay)
                        } else {
                            // Not enough momentum — manually animate to full close with linear speed
                            val distance = sheetHeight - rawOffset
                            val safeVelocity = available.y.takeIf { it > 0f } ?: 1f
                            val durationMs = ((distance / safeVelocity) * 1000).toInt().coerceIn(1, 10000)

                            closingTime.value = durationMs

                            animatedOffset.animateTo(sheetHeight, animationSpec = tween(durationMillis = durationMs, easing = LinearEasing))
                        }
                    }
                }

                return Velocity.Zero
            }
        }
    }

    // ----------------------------------------------------------
    // To handle drags directly on the background/sheet
    // ----------------------------------------------------------
    val pointerInputModifier = Modifier.pointerInput(Unit) {
        val speeds = SpeedCollector()

        detectVerticalDragGestures(
            onDragStart = {
                isDragging = true
                speeds.completeForAverage()
            },
            onVerticalDrag = { change, delta ->
                change.consume()
                rawOffset = (rawOffset + delta).coerceAtLeast(0f)

                val timeDeltaMs = maxOf(change.uptimeMillis - change.previousUptimeMillis, 1)
                val speed = delta / timeDeltaMs * 1000f // px per second

                speeds.collect(speed)

                coroutineScope.launch {
                    animatedOffset.snapTo(rawOffset)
                }
            },
            onDragEnd = {
                isDragging = false

                coroutineScope.launch {
                    // Snap to current raw offset before animating
                    animatedOffset.snapTo(rawOffset)

                    val velocity = speeds.completeForAverage()

                    // -------------------------------------------------------------------------
                    // Collapse if dragged enough distance in total, or if dragged fast enough
                    // -------------------------------------------------------------------------
                    // NOTE: More strict than nested scroll, so reduce the threshold for a similar experience


                    if (rawOffset >  sheetHeightPx.intValue * sheetDragThreshold || velocity > velocityThreshold) {
                        val sheetHeight = sheetHeightPx.intValue.toFloat()

                        onClose()

                        // Try to use a natural decay (if the velocity is high enough for the sheet to leave)
                        val decay = exponentialDecay<Float>()
                        val target = decay.calculateTargetValue(rawOffset, velocity)

                        if (target >= sheetHeight) {
                            // Safe to decay — it'll go off screen
                            closingTime.value = estimateDecayTimeToTarget(rawOffset, velocity, sheetHeight)
                            animatedOffset.animateDecay(velocity, decay)
                        } else {
                            // Not enough momentum — manually animate to full close with linear speed
                            val distance = sheetHeight - rawOffset
                            val safeVelocity = velocity.takeIf { it > 0f } ?: 1f
                            val durationMs = ((distance / safeVelocity) * 1000).toInt().coerceIn(1, 10000)

                            closingTime.value = durationMs

                            animatedOffset.animateTo(sheetHeight, animationSpec = tween(durationMillis = durationMs))
                        }
                    } else {
                        animatedOffset.animateTo(0f, animationSpec = sheetAnimationSpec)
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

    // ----------------------------------------------------------
    // The actual sheet
    // ----------------------------------------------------------
    Surface(
        modifier = modifier
            .widthIn(max = maxWidth)
            .wrapContentHeight()
            .offset { IntOffset(0, offsetToUse.roundToInt()) }
            .onGloballyPositioned { coordinates ->
                sheetHeightPx.intValue = coordinates.size.height
            }
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .then(
                if (locked) {
                    Modifier
                } else {
                    pointerInputModifier.nestedScroll(nestedScrollConnection)
                }
            ),
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        content()
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

fun estimateDecayTimeToTarget(
    startOffset: Float,
    velocity: Float,
    targetOffset: Float
): Int? {
    if (velocity == 0f) return null

    val decayRate = 8.0f // Used by compose

    val delta = targetOffset - startOffset
    val ratio = 1f - (delta * decayRate / velocity)

    if (ratio <= 0f || ratio >= 1f) return null

    val t = -ln(ratio) / decayRate
    return (t * 1000).roundToInt()
}