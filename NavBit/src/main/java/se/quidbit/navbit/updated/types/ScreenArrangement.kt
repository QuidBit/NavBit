package se.quidbit.navbit.updated.types

import androidx.compose.runtime.Composable
import se.quidbit.navbit.toimplement.NavBitNavigationState
import se.quidbit.navbit.types.TransitionDirection

// Keep a single global transition for all screens displayed - Can be moved to per screen if deemed necessary later on but I don't think it should be
// NOTE: The transition specified is the one when we enter the screen, as well as the one when we leave to make it match (perhaps also needs expansion...)
    // Meaning that the transition property of the root state will always be ignored

data class ScreenArrangement (
    val main : ScreenComposable,
    val transition : NewTransition = NewTransition.Fade,
    val overlays : List<OverlayScreen> = ArrayList()
) {
    companion object {
        fun main(id : String, compose : @Composable () -> Unit) : ScreenArrangement {
            return ScreenArrangement(ScreenComposable(id, compose))
        }
    }
}

data class OverlayScreen (
    val overlayTYpe : ScreenOverlayType,
    val screen : ScreenComposable
)

enum class ScreenOverlayType {
    Sheet,
    Popup
}

sealed class NewTransition {
    data object Fade : NewTransition()
    class Slide(val direction: TransitionDirection) : NewTransition()
}

data class ScreenComposable (
    val id : String,
    val screen : @Composable () -> Unit
)

// ------------------------------------------------------------
abstract class ScreenArrangementHandler {
    abstract fun <T : NavBitNavigationState>getScreenArrangement(state : NavBitNavigationState) : ScreenArrangement

    fun getCurrentTransition(newArrangement: ScreenArrangement, oldArrangement: ScreenArrangement, direction: TransitionDirection) : NewTransition {
        return when (direction) {
            TransitionDirection.Forward -> newArrangement.transition
            TransitionDirection.Backward -> oldArrangement.transition
        }
    }
}