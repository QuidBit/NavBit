package se.quidbit.navbit.updated.toimplement

import android.app.Activity
import se.quidbit.navbit.toimplement.NavBitInteraction
import se.quidbit.navbit.toimplement.NavBitNavigationState
import se.quidbit.navbit.types.InteractionResult

abstract class NewBitInteractionHandler<I : NavBitInteraction,  S : NavBitNavigationState> {
    abstract fun getBackInteraction() : I
    abstract fun applyInteractionOnState(i: I, s : S, activity: Activity) : InteractionResult<S>
    abstract fun applyBackInteractionOnState(s : S) : InteractionResult<S>
}