package se.quidbit.navbit.types

import androidx.compose.animation.core.Easing
import se.quidbit.navbit.internal.ScreenChange

abstract class ScreenTransition {
    abstract fun durationMs() : Int
    abstract fun easing() : Easing
    abstract fun start(screenChange: ScreenChange, width: Float): TransitionTransform
    abstract fun end(screenChange: ScreenChange, width: Float): TransitionTransform
    abstract fun shouldSnap(current : TransitionTransform, new : TransitionTransform, screenChange: ScreenChange, width: Float) : Boolean

    abstract fun enteringOnTop() : Boolean

    fun isOnTop(screenChange: ScreenChange) : Boolean {
        return screenChange == ScreenChange.Entering && enteringOnTop() || screenChange == ScreenChange.Leaving && !enteringOnTop()
    }
}

data class TransitionTransform(
    val offset: Float = 0f,
    val scale: Float = 1f,
    val alpha: Float = 1f,
)

