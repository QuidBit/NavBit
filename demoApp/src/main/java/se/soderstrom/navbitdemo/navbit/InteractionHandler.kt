package se.soderstrom.navbitdemo.navbit

import android.app.Activity
import se.quidbit.navbit.InteractionResult
import se.quidbit.navbit.NavBitInteractionHandler
import se.quidbit.navbit.TransitionDirection

class InteractionHandler : NavBitInteractionHandler<Interaction, NavigationState>() {
    override fun applyBackInteractionOnState(s: NavigationState): InteractionResult<NavigationState> {
        val newState = when (s) {
            NavigationState.Start -> return InteractionResult.CloseApp()
            NavigationState.Info -> NavigationState.Start
            NavigationState.InfoDetails -> NavigationState.Info
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
            is Interaction.ViewInfo -> when (s) {
                is NavigationState.Start -> NavigationState.Info
                else -> return InteractionResult.Unhandled()
            }
            is Interaction.ViewInfoDetails -> when (s) {
                is NavigationState.Info -> NavigationState.InfoDetails
                else -> return InteractionResult.Unhandled()
            }
            is Interaction.Done -> when (s) {
                is NavigationState.Info,
                is NavigationState.InfoDetails -> {
                    transition = TransitionDirection.Backward
                    NavigationState.Start
                }
                else -> return InteractionResult.Unhandled()
            }
        }

        return InteractionResult.NewState(newState, transition)
    }

    override fun getBackInteraction(): Interaction {
        return Interaction.Back()
    }
}