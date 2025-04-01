package se.soderstrom.navbitdemo.navbit

import android.content.Context
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import se.quidbit.navbit.toimplement.AppTheme
import se.quidbit.navbit.toimplement.NavBitScreenHandler
import se.quidbit.navbit.types.TransitionFade
import se.quidbit.navbit.types.ScreenArrangement
import se.quidbit.navbit.types.ScreenOverlayType
import se.quidbit.navbit.types.ScreenResult
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
    override fun screenFromNavigationState(
        context: Context,
        s: NavigationState
    ): ScreenResult {
        val arrangement = when (s) {
            is StartBasedState -> {

                val screenArrangement = ScreenArrangement("StartScreen") {
                    StartScreen(s.count)
                }

                // Detail Sheets
                // ---------------------------
                if (s is NavigationState.Info || s is NavigationState.InfoDetails) {
                    screenArrangement.addOverlay(ScreenOverlayType.Sheet, "SheetsInfoScreen") { SheetsInfoScreen() }
                }
                if (s is NavigationState.InfoDetails) {
                    screenArrangement.addOverlay(ScreenOverlayType.Sheet, "SheetsInfoDetailsScreen") { SheetsInfoDetailsScreen(s.expanded) }
                }

                // Clear Popups
                // ---------------------------
                if (s is NavigationState.ClearCheck) {
                    screenArrangement.addOverlay(ScreenOverlayType.Popup, "PopupClearScreen") { PopupClearScreen() }
                }
                if (s is NavigationState.ClearCheckAgain) {
                    screenArrangement.addOverlay(ScreenOverlayType.Popup, "PopupClearAgainScreen") { PopupClearAgainScreen() }
                }

                screenArrangement
            }

            is NavigationState.ScreenA -> ScreenArrangement("ScreenA") { ScreenA(s.count) }
            is NavigationState.ScreenB -> ScreenArrangement("ScreenB") { ScreenB() }
        }

        return ScreenResult.Arrangement(arrangement)
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

        return result ?: TransitionFade()
    }

    override fun getTheme(context: Context, isDarkMode: Boolean): AppTheme {
        val typography = Typography(
            bodyLarge = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
            )
        )

        // NOTE: Identical content, but required to duplicate to get the correct default colors
        val light = lightColorScheme(
            primary = Color(ContextCompat.getColor(context, R.color.demo_teal_200)),
            secondary = Color(ContextCompat.getColor(context, R.color.demo_teal_700)),
            tertiary = Color(ContextCompat.getColor(context, R.color.demo_red)),
        )
        val dark = darkColorScheme(
            primary = Color(ContextCompat.getColor(context, R.color.demo_teal_200)),
            secondary = Color(ContextCompat.getColor(context, R.color.demo_teal_700)),
            tertiary = Color(ContextCompat.getColor(context, R.color.demo_red)),
        )

        return AppTheme (
            typography = typography,
            colorScheme = if (isDarkMode) dark else light,
            holderBackgroundColor = Color(ContextCompat.getColor(context, R.color.holder_background))
        )
    }

    override fun maxOverlayCount(): Int {
        return 2
    }
}