package se.quidbit.navbit.types

import se.quidbit.navbit.toimplement.NavBitNavigationState

/**
 * A sealed class representing the result of an interaction.
 * It defines multiple possible outcomes, such as updating the state, ignoring interactions, etc
 */
sealed class InteractionResult <S : NavBitNavigationState> {
    /**
     * Used when the view should not be updated by an interaction, for example, ignoring no longer relevant API calls coming in.
     */
    class Ignore<S : NavBitNavigationState> : InteractionResult<S>()
    class ToDo<S : NavBitNavigationState> : InteractionResult<S>()
    class Unexpected<S : NavBitNavigationState> : InteractionResult<S>()
    class ErrorRead<S : NavBitNavigationState>(var error : ReadError) : InteractionResult<S>()
    class CloseApp<S : NavBitNavigationState> : InteractionResult<S>()

    /**
     * Used when the app content has been modified  (and consequently the UI might need updating)
     */
    class Complete<S : NavBitNavigationState>(var state : S) : InteractionResult<S>()
}