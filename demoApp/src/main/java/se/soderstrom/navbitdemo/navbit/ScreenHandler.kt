package se.soderstrom.navbitdemo.navbit

import android.content.Context
import se.quidbit.navbit.NavBitNavigationState
import se.quidbit.navbit.NavBitScreenData
import se.quidbit.navbit.NavBitScreenHandler
import se.quidbit.navbit.Screen
import se.quidbit.navbit.ScreenDataResult
import se.quidbit.navbit.ScreenType
import se.quidbit.navbit.TransitionDirection
import se.quidbit.navbit.TransitionType
import se.soderstrom.navbitdemo.screens.PopupClearScreen
import se.soderstrom.navbitdemo.screens.SheetsInfoDetailsScreen
import se.soderstrom.navbitdemo.screens.SheetsInfoScreen
import se.soderstrom.navbitdemo.screens.StartScreen
import se.soderstrom.navbitdemo.screens.TimerScreen

class ScreenHandler : NavBitScreenHandler<ScreenData>() {
    override fun generateNewScreen(
        context: Context,
        screenData: ScreenData,
        type: ScreenType
    ): Screen<*> {
        return when (screenData) {
            is ScreenData.Start -> StartScreen(context)
            is ScreenData.ClearCheck -> PopupClearScreen(context)
            is ScreenData.Info -> SheetsInfoScreen(context)
            is ScreenData.InfoDetails -> SheetsInfoDetailsScreen(context)
            is ScreenData.Timer -> TimerScreen(context)
        }
    }

    override fun screenDataDeepCopy(t: NavBitScreenData): ScreenData {
        val t = t as ScreenData
        return when(t) {
            is ScreenData.Start -> ScreenData.Start(t.count)
            is ScreenData.ClearCheck -> ScreenData.ClearCheck
            is ScreenData.Info -> ScreenData.Info
            is ScreenData.InfoDetails -> ScreenData.InfoDetails
            is ScreenData.Timer -> ScreenData.Timer
        }
    }

    override fun screenDataFromNavigationState(
        s: NavBitNavigationState,
        context: Context
    ): ScreenDataResult<ScreenData> {
        val s = s as NavigationState
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
                ScreenData.InfoDetails
            }
            is NavigationState.Timer -> ScreenData.Timer
        }

        return ScreenDataResult.Success(screenData, screenType)
    }

    override fun getFullScreenTransitionType(
        screenData: ScreenData,
        direction: TransitionDirection
    ): TransitionType.Full {
        return TransitionType.Full.Slide
    }
}