package se.soderstrom.navbitdemo.navbit

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import se.quidbit.navbit.toimplement.NavBitScreenHandler
import se.quidbit.navbit.types.TransitionFade
import se.quidbit.navbit.types.ScreenArrangement
import se.quidbit.navbit.types.ScreenOverlayType
import se.quidbit.navbit.types.ScreenTransition
import se.quidbit.navbit.types.TransitionSlide
import se.quidbit.navbit.types.TransitionDirection
import se.soderstrom.navbitdemo.R
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
    ): ScreenTransition {
        val result = when (old) {
            is NavigationState.Start -> when (new) {
                is NavigationState.ScreenA -> TransitionSlide(TransitionDirection.Forward)
                else -> null
            }
            is NavigationState.ClearCheck -> when (new) {
                is NavigationState.ClearCheckAgain -> TransitionSlide(TransitionDirection.Forward)
                else -> null
            }
            is NavigationState.ClearCheckAgain -> TransitionSlide(TransitionDirection.Backward)
            is NavigationState.Info -> when (new) {
                is NavigationState.InfoDetails -> TransitionSlide(TransitionDirection.Forward)
                else -> null
            }
            is NavigationState.InfoDetails -> TransitionSlide(TransitionDirection.Backward)
            is NavigationState.ScreenA -> when (new) {
                is NavigationState.Start -> TransitionSlide(TransitionDirection.Backward)
                is NavigationState.ScreenB -> TransitionSlide(TransitionDirection.Forward)
                else -> null
            }
            is NavigationState.ScreenB -> TransitionSlide(TransitionDirection.Backward)
        }

        return result ?: TransitionFade
    }

    override fun holderBackgroundColor(context : Context): Color {
        return Color(ContextCompat.getColor(context, R.color.demo_grey))
    }

    override fun screenBackgroundColor(context : Context): Color {
        return Color(ContextCompat.getColor(context, R.color.demo_white))
    }
}