package se.quidbit.navbit.toimplement

import android.app.Activity
import se.quidbit.navbit.types.InteractionResult

abstract class NavBitInteractionHandler<T : NavBitInteraction,  U : NavBitNavigationState> {
    abstract fun getBackInteraction() : T
    abstract fun applyInteractionOnState(i: T, s : U, activity: Activity) : InteractionResult<U>
    abstract fun applyBackInteractionOnState(s : U) : InteractionResult<U>
}