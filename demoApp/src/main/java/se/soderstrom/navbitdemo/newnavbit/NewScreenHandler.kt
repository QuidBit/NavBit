package se.soderstrom.navbitdemo.newnavbit

import android.content.Context
import androidx.compose.ui.graphics.Color
import se.quidbit.navbit.updated.toimplement.NewBitScreenHandler
import se.quidbit.navbit.updated.types.InteractionReceiver
import se.quidbit.navbit.updated.types.ScreenArrangement
import se.quidbit.navbit.updated.types.ScreenOverlayType
import se.quidbit.navbit.updated.types.ScreenTransitionSet
import se.quidbit.navbit.updated.types.TransitionFade
import se.quidbit.navbit.updated.types.TransitionSlideBack
import se.quidbit.navbit.updated.types.TransitionSlideForward
import se.soderstrom.navbitdemo.newscreen.PopupClearAgainScreen
import se.soderstrom.navbitdemo.newscreen.PopupClearScreen
import se.soderstrom.navbitdemo.newscreen.ScreenA
import se.soderstrom.navbitdemo.newscreen.ScreenB
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
            is NavigationState.Start -> ScreenArrangement(
                "StartScreen") { StartScreen(i, s.count) }
            is NavigationState.ClearCheck -> ScreenArrangement(
                "StartScreen", { StartScreen(i, s.count) },
                ScreenOverlayType.Popup, "PopupClearScreen", { PopupClearScreen(i) },
            )
            is NavigationState.ClearCheckAgain -> ScreenArrangement(
                "StartScreen", { StartScreen(i, s.count) },
                ScreenOverlayType.Popup,"PopupClearAgainScreen", { PopupClearAgainScreen(i) },
            )
            is NavigationState.Info -> ScreenArrangement(
                "StartScreen", { StartScreen(i, s.count) },
                ScreenOverlayType.Sheet,"SheetsInfoScreen", { SheetsInfoScreen(i) }
            )
            is NavigationState.InfoDetails -> ScreenArrangement(
                "StartScreen", { StartScreen(i, s.count) },
                ScreenOverlayType.Sheet, "SheetsInfoScreen", { SheetsInfoScreen(i) },
                ScreenOverlayType.Sheet, "SheetsInfoDetailScreen") { SheetsInfoDetailsScreen(i, s.expanded) }
            is NavigationState.ScreenA -> ScreenArrangement(
                "ScreenA") { ScreenA(i, s.count) }
            is NavigationState.ScreenB -> ScreenArrangement(
                "ScreenB") { ScreenB(i) }
        }
    }

    override fun transitionFromNavigationStates(
        old: NavigationState,
        new: NavigationState
    ): ScreenTransitionSet {
        val result = when (old) {
            is NavigationState.Start -> when (new) {
                is NavigationState.ScreenA -> TransitionSlideForward
                else -> null
            }
            is NavigationState.ClearCheck -> when (new) {
                is NavigationState.ClearCheckAgain -> TransitionSlideForward
                else -> null
            }
            is NavigationState.ClearCheckAgain -> TransitionSlideBack
            is NavigationState.Info -> when (new) {
                is NavigationState.InfoDetails -> TransitionSlideForward
                else -> null
            }
            is NavigationState.InfoDetails -> TransitionSlideBack
            is NavigationState.ScreenA -> when (new) {
                is NavigationState.Start -> TransitionSlideBack
                is NavigationState.ScreenB -> TransitionSlideForward
                else -> null
            }
            is NavigationState.ScreenB -> TransitionSlideBack
        }

        return result ?: TransitionFade
    }

    override fun mainBackgroundColor(): Color {
        return Color.Gray
    }
}