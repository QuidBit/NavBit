package se.quidbit.navbit.updated.toimplement

import android.content.Context
import se.quidbit.navbit.toimplement.NavBitInteraction
import se.quidbit.navbit.toimplement.NavBitNavigationState
import se.quidbit.navbit.updated.types.InteractionReceiver
import se.quidbit.navbit.updated.types.ScreenArrangement

abstract class NewBitScreenHandler<I : NavBitInteraction, S : NavBitNavigationState> {
    abstract fun screenArrangementFromNavigationState(s : S, i : InteractionReceiver<I>, context : Context) : ScreenArrangement
}