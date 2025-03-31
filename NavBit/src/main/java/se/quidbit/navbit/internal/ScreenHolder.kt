package se.quidbit.navbit.internal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import se.quidbit.navbit.types.ScreenComposable
import se.quidbit.navbit.types.ScreenTransition

@Composable
internal fun ScreenHolder(
    newScreenComposable: ScreenComposable,
    transition: ScreenTransition
) {
    val screenSlots by remember { mutableStateOf(ScreenSlots(newScreenComposable)) }

    val currentScreenComposable = when (screenSlots.currentIsA()) {
        true -> screenSlots.contentSlotA
        false -> screenSlots.contentSlotB
    }

    if (currentScreenComposable?.id != newScreenComposable.id) {
        screenSlots.currentSlot = screenSlots.currentSlot.opposite()
        screenSlots.transitionCounter++
    }

    when (screenSlots.currentSlot) {
        ScreenSlot.SlotA -> screenSlots.contentSlotA = newScreenComposable
        ScreenSlot.SlotB -> screenSlots.contentSlotB = newScreenComposable
    }

    // Box to contain the screen slots
    Box(modifier = Modifier.wrapContentSize()) {
        screenSlots.contentSlotB?.let {
            JumpAndAnimateBox(
                screenSlots.transitionCounter,
                it,
                transition,
                ScreenChange.entering(!screenSlots.currentIsA())
            )
        }

        // NOTE: Slot A is placed last to make sure it is on top in the beginning, when no secondary slot is visible
            // Otherwise, slotB becomes visible for a split second when it is added
        JumpAndAnimateBox(
            screenSlots.transitionCounter,
            screenSlots.contentSlotA,
            transition,
            ScreenChange.entering(screenSlots.currentIsA())
        )
    }
}

@Composable
fun JumpAndAnimateBox(counter: Int, composable : ScreenComposable, transition: ScreenTransition, screenChange: ScreenChange) {

    val animation = remember { Animatable(TransitionAnimation(), TransitionAnimationTypeConverter()) }

    val screenWidthDp = LocalConfiguration.current.screenWidthDp.toFloat()

    // Update the offset instantly when the counter changes
    LaunchedEffect(counter) {

        // NOTE: To avoid glitching during fades, the zIndex must be set at the same time as the other properties
        // (even though it is not actually animating)
        val z = if (transition.isOnTop(screenChange)) 1f else 0f

        val start = transition.start(screenChange, screenWidthDp)

        // Conditional snapping
        // -------------------------------
        // Makes sure the current screen is animated in a flow without jumping for moving transitions
        // However, the screen coming in might appear directly in the position of the old slot, essentially a swap in view
        // But, realistically no one will notice the difference anyway with normal transition speeds
        if (transition.shouldSnap(animation.value.toTransform(), start, screenChange, screenWidthDp)) {
            animation.snapTo(TransitionAnimation.new(z, start))
        }

        animation.animateTo(
            TransitionAnimation.new(z, transition.end(screenChange, screenWidthDp)),
            tween(durationMillis = transition.durationMs())
        )
    }

    Surface(
        modifier = Modifier
            .wrapContentSize()
            .offset(x = animation.value.offset.dp, y = 0.dp)
            .alpha(animation.value.alpha)
            .scale(animation.value.scale)
            .zIndex(animation.value.z)
            .background(MaterialTheme.colorScheme.background) // Enforce a background on all screens, since it is required for correct fade animations right now
    ) {
        composable.content.invoke()
    }
}

data class ScreenSlots(
    var contentSlotA: ScreenComposable,
    var contentSlotB: ScreenComposable? = null,
    var currentSlot: ScreenSlot = ScreenSlot.SlotA,
    var transitionCounter : Int = 0
) {
    fun currentIsA() : Boolean {
        return currentSlot == ScreenSlot.SlotA
    }
}

enum class ScreenSlot {
    SlotA, SlotB;

    fun opposite(): ScreenSlot {
        return when (this) {
            SlotA -> SlotB
            SlotB -> SlotA
        }
    }
}

enum class ScreenChange {
    Entering,
    Leaving;

    companion object {
        fun entering(entering : Boolean) : ScreenChange {
            return if (entering) Entering else Leaving
        }
    }
}