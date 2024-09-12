package se.quidbit.navbit.types

import androidx.compose.ui.unit.IntOffset

val TransitionSlideBack = ScreenTransitionSet(TransitionDirection.Backward, StandardTransitions.SlideTransition)
val TransitionSlideForward = ScreenTransitionSet(TransitionDirection.Forward, StandardTransitions.SlideTransition)
val TransitionFade = ScreenTransitionSet(TransitionDirection.Forward, StandardTransitions.FadeTransition)

data class ScreenTransitionSet (
    val direction: TransitionDirection,
    val transition: NewScreenTransition
)

abstract class NewScreenTransition {
    abstract fun offset(direction : TransitionDirection, width: Int, height: Int): IntOffset
    abstract fun alpha(direction : TransitionDirection): Float
    abstract fun scale(direction : TransitionDirection): Float
}

object StandardTransitions {
    object SlideTransition : NewScreenTransition() {
        override fun offset(direction : TransitionDirection, width: Int, height: Int): IntOffset {
            return when (direction) {
                TransitionDirection.Forward -> IntOffset(-width, 0)
                TransitionDirection.Backward -> IntOffset(width, 0)
            }
        }
        override fun alpha(direction : TransitionDirection): Float { return 0f }
        override fun scale(direction : TransitionDirection): Float { return 0.8f }
    }

    object FadeTransition : NewScreenTransition() {
        override fun offset(direction : TransitionDirection, width: Int, height: Int): IntOffset { return IntOffset(0, 0) }
        override fun alpha(direction : TransitionDirection): Float { return 0f }
        override fun scale(direction : TransitionDirection): Float { return 1f }
    }
}

