package se.soderstrom.navbitdemo.newnavbit

import android.content.Context
import se.quidbit.navbit.types.TransitionDirection
import se.quidbit.navbit.updated.toimplement.NewBitScreenHandler
import se.quidbit.navbit.updated.types.InteractionReceiver
import se.quidbit.navbit.updated.types.ScreenArrangement
import se.quidbit.navbit.updated.types.ScreenTransitionSet
import se.quidbit.navbit.updated.types.StandardTransitions
import se.soderstrom.navbitdemo.newscreen.PopupClearScreen
import se.soderstrom.navbitdemo.newscreen.SheetsInfoDetailsScreen
import se.soderstrom.navbitdemo.newscreen.StartScreen
import se.soderstrom.navbitdemo.newscreen.SheetsInfoScreen

class NewScreenHandler : NewBitScreenHandler<Interaction, NavigationState>() {
    override fun screenArrangementFromNavigationState(
        s: NavigationState,
        i: InteractionReceiver<Interaction>,
        context: Context
    ): ScreenArrangement {
        return when (s) {
            is NavigationState.Start -> ScreenArrangement.main("StartScreen") {
                StartScreen(i, s.count)
            }
            is NavigationState.ClearCheck -> ScreenArrangement.main("PopupClearScreen") {
                PopupClearScreen({i.send(Interaction.ClearPerform)}, {i.send(Interaction.Back)})
            }
            is NavigationState.Fast -> TODO()
            is NavigationState.Info -> ScreenArrangement.main("SheetsInfoScreen") {
                SheetsInfoScreen({ i.send(Interaction.Increment) }, { i.send(Interaction.Back)})
            }
            is NavigationState.InfoDetails -> ScreenArrangement.main("SheetsInfoDetailScreen") {
                SheetsInfoDetailsScreen({}, {}, s.expanded)
            }
            is NavigationState.Slow -> TODO()
            is NavigationState.Timer -> TODO()
        }
    }

    override fun transitionFromNavigationStates(
        old: NavigationState,
        new: NavigationState
    ): ScreenTransitionSet {
        val result = when (old) {
            is NavigationState.Start -> when (new) {
                is NavigationState.ClearCheck -> ScreenTransitionSet(TransitionDirection.Forward, StandardTransitions.FadeTransition)
                is NavigationState.Info -> ScreenTransitionSet(TransitionDirection.Forward, StandardTransitions.SlideTransition)
                else -> null
            }
            is NavigationState.ClearCheck -> ScreenTransitionSet(TransitionDirection.Backward, StandardTransitions.FadeTransition)
            is NavigationState.Info -> ScreenTransitionSet(TransitionDirection.Backward, StandardTransitions.SlideTransition)

            is NavigationState.Fast -> null
            is NavigationState.InfoDetails -> null
            is NavigationState.Slow -> null
            is NavigationState.Timer -> null
        }

        return result ?: ScreenTransitionSet()
    }
}