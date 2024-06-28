package se.quidbit.navbit

sealed class InteractionResult <T : NavBitNavigationState> {
    class None<T : NavBitNavigationState> : InteractionResult<T>()
    class Unhandled<T : NavBitNavigationState> : InteractionResult<T>()
    class ErrorRead<T : NavBitNavigationState>(var error : ReadError) : InteractionResult<T>()
    class NewState<T : NavBitNavigationState>(var state : T, var direction : TransitionDirection) : InteractionResult<T>()
    class CloseApp<T : NavBitNavigationState> : InteractionResult<T>()
}