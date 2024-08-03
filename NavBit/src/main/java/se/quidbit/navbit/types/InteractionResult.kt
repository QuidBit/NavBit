package se.quidbit.navbit.types

import se.quidbit.navbit.toimplement.NavBitNavigationState

sealed class InteractionResult <S : NavBitNavigationState> {
    class Ignore<S : NavBitNavigationState> : InteractionResult<S>()
    class Unexpected<S : NavBitNavigationState> : InteractionResult<S>()
    class ErrorRead<S : NavBitNavigationState>(var error : ReadError) : InteractionResult<S>()
    class NewState<S : NavBitNavigationState>(var state : S, var direction : TransitionDirection) : InteractionResult<S>()
    class CloseApp<S : NavBitNavigationState> : InteractionResult<S>()
}