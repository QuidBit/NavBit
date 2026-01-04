package se.quidbit.navbit.toimplement

import android.app.Activity
import se.quidbit.navbit.types.InteractionResult

abstract class NavBitInteractionHandler<I : NavBitInteraction,  S : NavBitNavigationState> {
    abstract fun showDebugToasts() : Boolean
    abstract fun applyBackInteractionOnState(s : S) : InteractionResult<S>
    abstract fun applyCloseInteractionOnState(s : S) : InteractionResult<S>
    abstract fun applyInteractionOnState(i: I, s : S, activity: Activity) : InteractionResult<S>
}