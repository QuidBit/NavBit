package se.quidbit.navbit.toimplement

import android.app.Activity
import se.quidbit.navbit.types.InteractionResult

abstract class NavBitInteractionHandler<I : NavBitInteraction,  S : NavBitNavigationState> {
    abstract fun showDebugToasts() : Boolean
    abstract fun applyInteractionOnState(i: I, s : S, activity: Activity) : InteractionResult<S>
    abstract fun applyBackInteractionOnState(s : S) : InteractionResult<S>
}