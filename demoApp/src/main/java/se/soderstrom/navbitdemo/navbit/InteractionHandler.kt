package se.soderstrom.navbitdemo.navbit

import android.app.Activity
import se.quidbit.navbit.types.InteractionResult
import se.quidbit.navbit.toimplement.NavBitInteractionHandler

class InteractionHandler : NavBitInteractionHandler<Interaction, NavigationState>() {
    override fun showDebugToasts(): Boolean {
        return true
    }

    override fun applyCloseInteractionOnState(s: NavigationState): InteractionResult<NavigationState> {

        // Everything behaves like a normal backing, except in one case, where we essentially back multiple steps
        if (s is NavigationState.ClearCheckAgain) {
            return InteractionResult.Complete(NavigationState.Start(s.count))
        }

        return applyBackInteractionOnState(s)
    }

    override fun applyBackInteractionOnState(s: NavigationState): InteractionResult<NavigationState> {
        val newState = when (s) {
            is NavigationState.Start -> return InteractionResult.CloseApp()
            is NavigationState.ClearCheck -> NavigationState.Start(s.count)
            is NavigationState.ClearCheckAgain -> NavigationState.ClearCheck(s.count)
            is NavigationState.Info -> NavigationState.Start(s.count)
            is NavigationState.InfoDetails -> NavigationState.Info(s.count)
            is NavigationState.ScreenA -> NavigationState.Start(s.count)
            is NavigationState.ScreenB -> NavigationState.ScreenA(s.count)
        }

        return InteractionResult.Complete(newState)
    }

    override fun applyInteractionOnState(
        i: Interaction,
        s: NavigationState,
        activity : Activity
    ): InteractionResult<NavigationState> {
        val newState = when (i) {
            is Interaction.ClearCheck ->  when (s) {
                is NavigationState.Start -> {
                    if (s.count > 0)
                        NavigationState.ClearCheck(s.count)
                    else
                        return InteractionResult.Ignore()
                }
                is NavigationState.ClearCheck -> NavigationState.ClearCheckAgain(s.count)
                else -> return InteractionResult.Unexpected()
            }
            is Interaction.ClearPerform ->  when (s) {
                is NavigationState.ClearCheckAgain -> {
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
            is Interaction.GoToNext -> when (s) {
                is NavigationState.Start -> NavigationState.ScreenA(s.count)
                is NavigationState.ScreenA -> NavigationState.ScreenB(s.count)
                else -> return InteractionResult.Unexpected()
            }
            is Interaction.Done -> when (s) {
                is NavigationState.InfoDetails -> NavigationState.Start(s.count)
                is NavigationState.ScreenB -> NavigationState.Start(s.count)
                else -> return InteractionResult.Unexpected()
            }
            is Interaction.Increment -> when (s) {
                is NavigationState.Start -> s.copy(count = s.count + 1)
                is NavigationState.Info -> s.copy(count = s.count + 1)
                is NavigationState.ScreenA -> s.copy(count = s.count + 1)
                else -> return InteractionResult.Unexpected()
            }
            is Interaction.Expand -> when (s) {
                is NavigationState.InfoDetails -> s.copy(expanded = !s.expanded)
                else -> return InteractionResult.Unexpected()
            }
        }

        return InteractionResult.Complete(newState)
    }
}