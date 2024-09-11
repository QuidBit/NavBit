package se.quidbit.navbit.toimplement

import android.content.Context
import androidx.compose.ui.graphics.Color
import se.quidbit.navbit.types.InteractionReceiver
import se.quidbit.navbit.types.ScreenArrangement
import se.quidbit.navbit.types.ScreenTransitionSet

abstract class NavBitScreenHandler<I : NavBitInteraction, S : NavBitNavigationState> {
    abstract fun screenArrangementFromNavigationState(s : S, i : InteractionReceiver<I>, context : Context) : ScreenArrangement
    abstract fun transitionFromNavigationStates(old : S, new : S) : ScreenTransitionSet
    abstract fun mainBackgroundColor() : Color
}