package se.quidbit.navbit.updated.toimplement

import android.content.Context
import androidx.compose.ui.graphics.Color
import se.quidbit.navbit.toimplement.NavBitInteraction
import se.quidbit.navbit.toimplement.NavBitNavigationState
import se.quidbit.navbit.updated.types.InteractionReceiver
import se.quidbit.navbit.updated.types.ScreenArrangement
import se.quidbit.navbit.updated.types.ScreenTransitionSet

abstract class NewBitScreenHandler<I : NavBitInteraction, S : NavBitNavigationState> {
    abstract fun screenArrangementFromNavigationState(s : S, i : InteractionReceiver<I>, context : Context) : ScreenArrangement
    abstract fun transitionFromNavigationStates(old : S, new : S) : ScreenTransitionSet
    abstract fun mainBackgroundColor() : Color
}