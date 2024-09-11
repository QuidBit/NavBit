package se.quidbit.navbit.toimplement

import se.quidbit.navbit.types.InteractionResult

abstract class NavBitInteractionHandler<I : NavBitInteraction,  S : NavBitNavigationState> {
    abstract fun getBackInteraction() : I
    abstract fun applyInteractionOnState(i: I, s : S) : InteractionResult<S>
    abstract fun applyBackInteractionOnState(s : S) : InteractionResult<S>
}