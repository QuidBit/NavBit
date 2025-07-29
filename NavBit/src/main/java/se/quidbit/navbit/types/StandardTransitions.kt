package se.quidbit.navbit.types

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import se.quidbit.navbit.internal.ScreenChange
import kotlin.math.abs

const val TRANSITION_DURATION_DEFAULT_MS = 250
val TRANSITION_EASING_DEFAULT = FastOutSlowInEasing
// ---------------------------------------------------------------------

class TransitionSlide(
    private val direction: TransitionDirection,
    private val duration: Int = TRANSITION_DURATION_DEFAULT_MS,
    private val easing : Easing = TRANSITION_EASING_DEFAULT
) : ScreenTransition() {
    override fun durationMs() : Int { return duration }
    override fun easing(): Easing { return easing }

    override fun start(screenChange: ScreenChange, width : Float): TransitionTransform {
        return when (screenChange) {
            ScreenChange.Entering -> when (direction) {
                TransitionDirection.Forward -> TransitionTransform(width, 0.95f, 0.2f)
                TransitionDirection.Backward -> TransitionTransform(-width, 0.95f, 0.2f)
            }
            ScreenChange.Leaving -> TransitionTransform()
        }
    }
    override fun end(screenChange: ScreenChange, width : Float): TransitionTransform {
        return when (screenChange) {
            ScreenChange.Entering -> TransitionTransform(0f, 1f, 1f)
            ScreenChange.Leaving -> when (direction) {
                TransitionDirection.Forward -> TransitionTransform(-width, 0.95f, 0.2f)
                TransitionDirection.Backward -> TransitionTransform(width, 0.95f, 0.2f)
            }
        }
    }

    override fun shouldSnap(
        current: TransitionTransform,
        new: TransitionTransform,
        screenChange: ScreenChange,
        width: Float
    ): Boolean {
        return abs(current.offset - new.offset) > width - 1f
    }

    override fun enteringOnTop(): Boolean {
        return direction == TransitionDirection.Forward
    }
}

// ---------------------------------------------------------------------

class TransitionFade(
    private val duration : Int = TRANSITION_DURATION_DEFAULT_MS,
    private val easing : Easing = TRANSITION_EASING_DEFAULT
) : ScreenTransition() {
    override fun durationMs() : Int { return duration }
    override fun easing(): Easing { return easing }

    override fun start(screenChange: ScreenChange, width : Float): TransitionTransform {
        return when (screenChange) {
            ScreenChange.Entering -> TransitionTransform(alpha = 0f)
            ScreenChange.Leaving -> TransitionTransform()
        }
    }

    override fun end(screenChange: ScreenChange, width : Float): TransitionTransform {
        return TransitionTransform()
    }

    override fun shouldSnap(
        current: TransitionTransform,
        new: TransitionTransform,
        screenChange: ScreenChange,
        width: Float
    ): Boolean {
        return true
    }

    override fun enteringOnTop(): Boolean {
        return true
    }
}

object TransitionNone : ScreenTransition() {
    override fun durationMs() : Int { return 0 }
    override fun easing(): Easing { return TRANSITION_EASING_DEFAULT }

    override fun start(screenChange: ScreenChange, width : Float): TransitionTransform {
        return TransitionTransform()
    }

    override fun end(screenChange: ScreenChange, width : Float): TransitionTransform {
        return TransitionTransform()
    }

    override fun shouldSnap(
        current: TransitionTransform,
        new: TransitionTransform,
        screenChange: ScreenChange,
        width: Float
    ): Boolean {
        return true
    }

    override fun enteringOnTop(): Boolean {
        return true
    }
}
