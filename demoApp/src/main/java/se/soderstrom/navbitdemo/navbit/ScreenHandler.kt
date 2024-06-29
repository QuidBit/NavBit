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
import se.soderstrom.navbitdemo.screens.InfoDetailsScreen
import se.soderstrom.navbitdemo.screens.InfoScreen
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
            ScreenData.Info::class.java.toString() -> InfoScreen(context)
            ScreenData.InfoDetails::class.java.toString() -> InfoDetailsScreen(context)

            // Supports only sheet

            // Supports both

            else -> throw IllegalStateException("Missing Screen for screenTag type: $screenTag")
        }
    }

    override fun screenDataDeepCopy(t: NavBitScreenData): ScreenData {
        val t = t as ScreenData
        return when(t) {
            ScreenData.Start -> ScreenData.Start
            ScreenData.Info -> ScreenData.Info
            ScreenData.InfoDetails -> ScreenData.InfoDetails
        }
    }

    override fun screenDataFromNavigationState(
        s: NavBitNavigationState,
        context: Context
    ): ScreenDataResult<ScreenData> {
        val s = s as NavigationState
        var screenType = ScreenType.Full

        val screenData = when (s) {
            NavigationState.Start -> ScreenData.Start
            NavigationState.Info -> {
                screenType = ScreenType.Sheet
                ScreenData.Info
            }
            NavigationState.InfoDetails -> {
                screenType = ScreenType.Sheet
                ScreenData.InfoDetails
            }
        }

        return ScreenDataResult.Success(screenData, screenType)
    }

    override fun getTransitionType(
        data: ScreenData,
        screenType: ScreenType,
        direction: TransitionDirection
    ): TransitionType {
        return when (screenType) {
            ScreenType.Sheet -> TransitionType.Sheet
            ScreenType.Full -> {
               when (data) {
                   else -> TransitionType.Slide
               }
            }

        }
    }
}