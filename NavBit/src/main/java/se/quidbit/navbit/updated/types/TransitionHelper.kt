package se.quidbit.navbit.updated.types

import androidx.compose.animation.*
import androidx.compose.animation.core.tween

object TransitionHelper {
    fun fadeTransition(): ContentTransform {
        return fadeIn(animationSpec = tween(5000)) togetherWith fadeOut(animationSpec = tween(5000))
    }

    fun slideInTransition(): ContentTransform {
        return slideInHorizontally { fullWidth -> fullWidth } + fadeIn() togetherWith
                slideOutHorizontally { fullWidth -> -fullWidth } + fadeOut()
    }

    fun slideOutTransition(): ContentTransform {
        return slideInHorizontally { fullWidth -> -fullWidth } + fadeIn() togetherWith
                slideOutHorizontally { fullWidth -> fullWidth } + fadeOut()
    }
}

