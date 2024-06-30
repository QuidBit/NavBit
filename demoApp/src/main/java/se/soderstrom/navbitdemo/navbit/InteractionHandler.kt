package se.soderstrom.navbitdemo.navbit

import android.app.Activity
import se.quidbit.navbit.InteractionResult
import se.quidbit.navbit.NavBitInteractionHandler
import se.quidbit.navbit.TransitionDirection

class InteractionHandler : NavBitInteractionHandler<Interaction, NavigationState>() {
    override fun applyBackInteractionOnState(s: NavigationState): InteractionResult<NavigationState> {
        val newState = when (s) {
            is NavigationState.Start -> return InteractionResult.CloseApp()
            is NavigationState.Info -> NavigationState.Start(0)
            is NavigationState.InfoDetails -> NavigationState.Info
        }

        return InteractionResult.NewState(newState, TransitionDirection.Backward)
    }

    override fun applyInteractionOnState(
        i: Interaction,
        s: NavigationState,
        activity: Activity
    ): InteractionResult<NavigationState> {
        // The default transition for any interaction is going forwards
        // If that is not the case (like on completed input), the value should be overridden on that interaction below
        var transition = TransitionDirection.Forward

        val newState = when (i) {
            is Interaction.Back -> return applyBackInteractionOnState(s)
            is Interaction.ViewSheetsInfo -> when (s) {
                is NavigationState.Start -> NavigationState.Info
                else -> return InteractionResult.Unhandled()
            }
            is Interaction.ViewSheetsInfoDetails -> when (s) {
                is NavigationState.Info -> NavigationState.InfoDetails
                else -> return InteractionResult.Unhandled()
            }
            is Interaction.Done -> when (s) {
                is NavigationState.Info,
                is NavigationState.InfoDetails -> {
                    transition = TransitionDirection.Backward
                    NavigationState.Start(0)
                }
                else -> return InteractionResult.Unhandled()
            }
            is Interaction.Increment -> when (s) {
                is NavigationState.Start -> NavigationState.Start(s.count + 1)
                else -> return InteractionResult.Unhandled()
            }
        }

        return InteractionResult.NewState(newState, transition)
    }

    override fun getBackInteraction(): Interaction {
        return Interaction.Back
    }
}