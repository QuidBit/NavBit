package se.quidbit.navbit.types

import se.quidbit.navbit.internal.ScreenChange
import kotlin.math.abs

const val TRANSITION_DURATION_DEFAULT_MS = 250

// ---------------------------------------------------------------------

class TransitionSlide(private val direction: TransitionDirection, private val duration: Int = TRANSITION_DURATION_DEFAULT_MS) : ScreenTransition() {
    override fun durationMs() : Int { return duration }

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

class TransitionFade(private val duration : Int = TRANSITION_DURATION_DEFAULT_MS) : ScreenTransition() {
    override fun durationMs() : Int { return duration }

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
