package se.quidbit.navbit.internal

import android.util.Log
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntOffset
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import se.quidbit.navbit.toimplement.NavBitInteraction
import se.quidbit.navbit.toimplement.NavBitNavigationState
import se.quidbit.navbit.types.ScreenComposable
import se.quidbit.navbit.types.ScreenTransitionSet

internal const val TRANSITION_TIME_MS = 500

@Composable
internal fun <I : NavBitInteraction, S : NavBitNavigationState>
    ScreenHolder(newScreenComposable: ScreenComposable, transitionSet: ScreenTransitionSet)
{
    val holderTransition = transitionSet.transition
    val direction = transitionSet.direction
    Log.e("NavBit", " -- IN SCREEN HOLDER")

    val screenSlots by remember { mutableStateOf(ScreenSlots(null, null, ScreenSlot.SlotA)) }

    // Flag for managing the visibility of the old screen
    var isAnimating by remember { mutableStateOf(false) }

    val oldScreenComposable = when (screenSlots.currentSlot) {
        ScreenSlot.SlotA -> screenSlots.contentSlotA
        ScreenSlot.SlotB -> screenSlots.contentSlotB
    }

    // Trigger animation only if the ID has changed
    if (oldScreenComposable?.id != newScreenComposable.id) {
        screenSlots.currentSlot = screenSlots.currentSlot.opposite()
        isAnimating = true
    }

    when (screenSlots.currentSlot) {
        ScreenSlot.SlotA -> screenSlots.contentSlotA = newScreenComposable
        ScreenSlot.SlotB -> screenSlots.contentSlotB = newScreenComposable
    }

    // Animation progress for the screen
    val transition = updateTransition(targetState = isAnimating, label = "")
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val screenWidth = with(LocalDensity.current) { screenWidthDp.dp.toPx() }
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val screenHeight = with(LocalDensity.current) { screenHeightDp.dp.toPx() }

    // Create the content transform equivalent
    val slotAOffset by transition.animateIntOffset(
        label = "slotAOffset",
        transitionSpec = { tween(durationMillis = TRANSITION_TIME_MS) }
    ) { isAnim ->
        if (isAnim && screenSlots.currentSlot != ScreenSlot.SlotA) holderTransition.offset(direction, screenWidth.toInt(), screenHeight.toInt())
        else IntOffset(0, 0)
    }

    val slotBOffset by transition.animateIntOffset(
        label = "slotBOffset",
        transitionSpec = { tween(durationMillis = TRANSITION_TIME_MS) }
    ) { isAnim ->
        if (isAnim && screenSlots.currentSlot != ScreenSlot.SlotB) holderTransition.offset(direction, screenWidth.toInt(), screenHeight.toInt())
        else IntOffset(0, 0)
    }

    val slotAAlpha by transition.animateFloat(
        label = "slotAAlpha",
        transitionSpec = { tween(durationMillis = TRANSITION_TIME_MS) }
    ) { isAnim ->
        if (isAnim && screenSlots.currentSlot != ScreenSlot.SlotA) holderTransition.alpha(direction)
        else 1f
    }

    val slotBAlpha by transition.animateFloat(
        label = "slotBAlpha",
        transitionSpec = { tween(durationMillis = TRANSITION_TIME_MS) }
    ) { isAnim ->
        if (isAnim && screenSlots.currentSlot != ScreenSlot.SlotB) holderTransition.alpha(direction.opposite())
        else 1f
    }

    val slotAScale by transition.animateFloat(
        label = "slotAScale",
        transitionSpec = { tween(durationMillis = TRANSITION_TIME_MS) }
    ) { isAnim ->
        if (isAnim && screenSlots.currentSlot != ScreenSlot.SlotA) holderTransition.scale(direction)
        else 1f
    }

    val slotBScale by transition.animateFloat(
        label = "slotBScale",
        transitionSpec = { tween(durationMillis = TRANSITION_TIME_MS) }
    ) { isAnim ->
        if (isAnim && screenSlots.currentSlot != ScreenSlot.SlotB) holderTransition.scale(direction.opposite())
        else 1f
    }

    // Box to contain the screen slots
    Box(modifier = Modifier.wrapContentSize()) {
        screenSlots.contentSlotA?.let {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .offset { slotAOffset }
                    .alpha(slotAAlpha)
                    .scale(slotAScale)
                    .zIndex(if (transitionSet.transition.nextOnTop(transitionSet.direction)) 1f else 0f)
            ) {
                it.content.invoke()
            }
        }

        screenSlots.contentSlotB?.let {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .offset { slotBOffset }
                    .alpha(slotBAlpha)
                    .scale(slotBScale)
                    .zIndex(if (!transitionSet.transition.nextOnTop(transitionSet.direction)) 1f else 0f)
            ) {
                it.content.invoke()
            }
        }
    }
}

data class ScreenSlots(
    var contentSlotA: ScreenComposable?,
    var contentSlotB: ScreenComposable?,
    var currentSlot: ScreenSlot
)

enum class ScreenSlot {
    SlotA, SlotB;

    fun opposite(): ScreenSlot {
        return when (this) {
            SlotA -> SlotB
            SlotB -> SlotA
        }
    }
}