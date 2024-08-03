package se.soderstrom.navbitdemo.navbit

import android.content.Context
import se.quidbit.navbit.types.TransitionDirection
import se.quidbit.navbit.types.TransitionType
import se.quidbit.navbit.toimplement.NavBitNavigationState
import se.quidbit.navbit.toimplement.NavBitScreenData
import se.quidbit.navbit.toimplement.NavBitScreenHandler
import se.quidbit.navbit.toimplement.NavBitScreen
import se.quidbit.navbit.types.ScreenDataResult
import se.quidbit.navbit.types.ScreenType
import se.soderstrom.navbitdemo.screens.FastScreen
import se.soderstrom.navbitdemo.screens.PopupClearScreen
import se.soderstrom.navbitdemo.screens.SheetsInfoDetailsScreen
import se.soderstrom.navbitdemo.screens.SheetsInfoScreen
import se.soderstrom.navbitdemo.screens.SlowScreen
import se.soderstrom.navbitdemo.screens.StartScreen
import se.soderstrom.navbitdemo.screens.TimerScreen

class ScreenHandler : NavBitScreenHandler<NavigationState, ScreenData>() {
    override fun generateNewScreen(
        context: Context,
        screenData: ScreenData,
        type: ScreenType
    ): NavBitScreen<*> {
        return when (screenData) {
            is ScreenData.Start -> StartScreen(context)
            is ScreenData.ClearCheck -> PopupClearScreen(context)
            is ScreenData.Info -> SheetsInfoScreen(context)
            is ScreenData.InfoDetails -> SheetsInfoDetailsScreen(context)
            is ScreenData.Timer -> TimerScreen(context)
            is ScreenData.Slow -> SlowScreen(context)
            is ScreenData.Fast -> FastScreen(context)
        }
    }

    override fun screenDataFromNavigationState(
        s: NavigationState,
        context: Context
    ): ScreenDataResult<ScreenData> {
        var screenType = ScreenType.Full

        val screenData = when (s) {
            is NavigationState.Start -> ScreenData.Start(s.count)
            is NavigationState.ClearCheck -> {
                screenType = ScreenType.PopUp
                ScreenData.ClearCheck
            }
            is NavigationState.Info -> {
                screenType = ScreenType.Sheet
                ScreenData.Info
            }
            is NavigationState.InfoDetails -> {
                screenType = ScreenType.Sheet
                ScreenData.InfoDetails(s.expanded)
            }
            is NavigationState.Timer -> ScreenData.Timer
            is NavigationState.Fast -> ScreenData.Fast
            is NavigationState.Slow -> ScreenData.Slow
        }

        return ScreenDataResult.Success(screenData, screenType)
    }

    override fun getFullScreenTransitionType(
        screenData: ScreenData,
        direction: TransitionDirection
    ): TransitionType.Full {
        return when (screenData) {
            is ScreenData.Start -> TransitionType.Full.Fade
            else -> TransitionType.Full.Slide
        }
    }
}