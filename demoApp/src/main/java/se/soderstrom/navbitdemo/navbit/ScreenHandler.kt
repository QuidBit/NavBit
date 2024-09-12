package se.soderstrom.navbitdemo.navbit

import android.content.Context
import androidx.compose.ui.graphics.Color
import se.quidbit.navbit.toimplement.NavBitScreenHandler
import se.quidbit.navbit.types.ScreenArrangement
import se.quidbit.navbit.types.ScreenOverlayType
import se.quidbit.navbit.types.ScreenTransitionSet
import se.quidbit.navbit.types.TransitionFade
import se.quidbit.navbit.types.TransitionSlideBack
import se.quidbit.navbit.types.TransitionSlideForward
import se.soderstrom.navbitdemo.screen.PopupClearAgainScreen
import se.soderstrom.navbitdemo.screen.PopupClearScreen
import se.soderstrom.navbitdemo.screen.ScreenA
import se.soderstrom.navbitdemo.screen.ScreenB
import se.soderstrom.navbitdemo.screen.SheetsInfoDetailsScreen
import se.soderstrom.navbitdemo.screen.StartScreen
import se.soderstrom.navbitdemo.screen.SheetsInfoScreen

class ScreenHandler : NavBitScreenHandler<NavigationState>() {
    override fun screenArrangementFromNavigationState(
        context: Context,
        s: NavigationState
    ): ScreenArrangement {
        return when (s) {
            is NavigationState.Start -> ScreenArrangement(
                "StartScreen") { StartScreen(s.count) }
            is NavigationState.ClearCheck -> ScreenArrangement(
                "StartScreen", { StartScreen(s.count) },
                ScreenOverlayType.Popup, "PopupClearScreen", { PopupClearScreen() },
            )
            is NavigationState.ClearCheckAgain -> ScreenArrangement(
                "StartScreen", { StartScreen(s.count) },
                ScreenOverlayType.Popup,"PopupClearAgainScreen", { PopupClearAgainScreen() },
            )
            is NavigationState.Info -> ScreenArrangement(
                "StartScreen", { StartScreen(s.count) },
                ScreenOverlayType.Sheet,"SheetsInfoScreen", { SheetsInfoScreen() }
            )
            is NavigationState.InfoDetails -> ScreenArrangement(
                "StartScreen", { StartScreen(s.count) },
                ScreenOverlayType.Sheet, "SheetsInfoScreen", { SheetsInfoScreen() },
                ScreenOverlayType.Sheet, "SheetsInfoDetailScreen") { SheetsInfoDetailsScreen(s.expanded) }
            is NavigationState.ScreenA -> ScreenArrangement(
                "ScreenA") { ScreenA(s.count) }
            is NavigationState.ScreenB -> ScreenArrangement(
                "ScreenB") { ScreenB() }
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