package se.quidbit.navbit.toimplement

import android.content.Context
import androidx.compose.ui.graphics.Color
import se.quidbit.navbit.types.ScreenArrangement
import se.quidbit.navbit.types.ScreenTransitionSet

abstract class NavBitScreenHandler<S : NavBitNavigationState> {
    abstract fun screenArrangementFromNavigationState(context : Context, s : S,) : ScreenArrangement
    abstract fun transitionFromNavigationStates(old : S, new : S) : ScreenTransitionSet
    abstract fun mainBackgroundColor() : Color
}