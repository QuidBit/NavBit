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
import se.soderstrom.navbitdemo.screens.SheetsInfoDetailsScreen
import se.soderstrom.navbitdemo.screens.SheetsInfoScreen
import se.soderstrom.navbitdemo.screens.StartScreen

class ScreenHandler : NavBitScreenHandler<ScreenData>() {
    override fun generateNewScreen(
        context: Context,
        screenTag: String,
        type: ScreenType
    ): Screen<*> {
        return when (screenTag) {
            // Supports only fullscreen
            ScreenData.Start::class.java.toString() -> StartScreen(context)
            ScreenData.Info::class.java.toString() -> SheetsInfoScreen(context)
            ScreenData.InfoDetails::class.java.toString() -> SheetsInfoDetailsScreen(context)

            // Supports only sheet

            // Supports both

            else -> throw IllegalStateException("Missing Screen for screenTag type: $screenTag")
        }
    }

    override fun screenDataDeepCopy(t: NavBitScreenData): ScreenData {
        val t = t as ScreenData
        return when(t) {
            is ScreenData.Start -> ScreenData.Start(t.count)
            is ScreenData.Info -> ScreenData.Info
            is ScreenData.InfoDetails -> ScreenData.InfoDetails
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
            is NavigationState.Info -> {
                screenType = ScreenType.Sheet
                ScreenData.Info
            }
            is NavigationState.InfoDetails -> {
                screenType = ScreenType.Sheet
                ScreenData.InfoDetails
            }
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