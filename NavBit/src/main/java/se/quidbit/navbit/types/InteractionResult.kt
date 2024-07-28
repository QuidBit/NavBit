package se.quidbit.navbit.types

import se.quidbit.navbit.toimplement.NavBitNavigationState

sealed class InteractionResult <T : NavBitNavigationState> {
    class Ignore<T : NavBitNavigationState> : InteractionResult<T>()
    class Unexpected<T : NavBitNavigationState> : InteractionResult<T>()
    class ErrorRead<T : NavBitNavigationState>(var error : ReadError) : InteractionResult<T>()
    class NewState<T : NavBitNavigationState>(var state : T, var direction : TransitionDirection) : InteractionResult<T>()
    class CloseApp<T : NavBitNavigationState> : InteractionResult<T>()
}