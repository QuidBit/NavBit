package se.soderstrom.navbitdemo.newnavbit

import android.app.Activity
import se.quidbit.navbit.types.InteractionResult
import se.quidbit.navbit.types.TransitionDirection
import se.quidbit.navbit.updated.toimplement.NewBitInteractionHandler

class InteractionHandler : NewBitInteractionHandler<Interaction, NavigationState>() {
    override fun applyBackInteractionOnState(s: NavigationState): InteractionResult<NavigationState> {
        val newState = when (s) {
            is NavigationState.Start -> return InteractionResult.CloseApp()
            is NavigationState.ClearCheck -> NavigationState.Start(s.count)
            is NavigationState.Info -> NavigationState.Start(s.count)
            is NavigationState.InfoDetails -> NavigationState.Info(s.count)
            is NavigationState.Timer -> NavigationState.Start(s.count)
            is NavigationState.Slow -> NavigationState.Start(s.count)
            is NavigationState.Fast -> NavigationState.Slow(s.count)
        }

        return InteractionResult.NewState(newState, TransitionDirection.Backward)
    }

    override fun applyInteractionOnState(
        i: Interaction,
        s: NavigationState,
        activity : Activity
    ): InteractionResult<NavigationState> {
        // The default transition for any interaction is going forwards
        // If that is not the case (like on completed input), the value should be overridden on that interaction below
        var transition = TransitionDirection.Forward

        val newState = when (i) {
            is Interaction.Back -> return applyBackInteractionOnState(s)
            is Interaction.ClearCheck ->  when (s) {
                is NavigationState.Start -> {
                    if (s.count > 0)
                        NavigationState.ClearCheck(s.count)
                    else
                        return InteractionResult.Ignore()
                }
                else -> return InteractionResult.Unexpected()
            }
            is Interaction.ClearPerform ->  when (s) {
                is NavigationState.ClearCheck -> {
                    transition = TransitionDirection.Backward
                    NavigationState.Start(0)
                }
                else -> return InteractionResult.Unexpected()
            }
            is Interaction.ViewSheetsInfo -> when (s) {
                is NavigationState.Start -> NavigationState.Info(s.count)
                else -> return InteractionResult.Unexpected()
            }
            is Interaction.ViewSheetsInfoDetails -> when (s) {
                is NavigationState.Info -> NavigationState.InfoDetails(s.count, false)
                else -> return InteractionResult.Unexpected()
            }
            is Interaction.GoToTimer -> when (s) {
                is NavigationState.Start -> NavigationState.Timer(s.count)
                else -> return InteractionResult.Unexpected()
            }
            is Interaction.GoToSlow -> when (s) {
                is NavigationState.Start -> NavigationState.Slow(s.count)
                else -> return InteractionResult.Unexpected()
            }
            is Interaction.Done -> when (s) {
                is NavigationState.InfoDetails -> {
                    transition = TransitionDirection.Backward
                    NavigationState.Start(s.count)
                }
                else -> return InteractionResult.Unexpected()
            }
            is Interaction.Increment -> when (s) {
                is NavigationState.Start -> s.copy(count = s.count + 1)
                is NavigationState.Info -> s.copy(count = s.count + 1)
                else -> return InteractionResult.Unexpected()
            }
            is Interaction.Expand -> when (s) {
                is NavigationState.InfoDetails -> s.copy(expanded = !s.expanded)
                is NavigationState.Slow -> NavigationState.Fast(s.count)
                else -> return InteractionResult.Unexpected()
            }
        }

        return InteractionResult.NewState(newState, transition)
    }

    override fun getBackInteraction(): Interaction {
        return Interaction.Back
    }
}