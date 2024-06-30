package se.quidbit.navbit

import android.content.Context

abstract class NavBitScreenHandler<T : NavBitScreenData> {
    abstract fun screenDataDeepCopy(t : NavBitScreenData) : T
    abstract fun screenDataFromNavigationState(s: NavBitNavigationState, context : Context) : ScreenDataResult<T>


    fun getScreenTransition(screenData: T, screenType : ScreenType, direction: TransitionDirection) : ScreenTransition {
        val transitionType = when (screenType) {
            ScreenType.Sheet -> TransitionType.Sheet
            else -> getFullScreenTransitionType(screenData, direction)
        }
        return ScreenTransition(transitionType, direction)
    }

    // NOTE: Should be based on NavigationState instead of screen data ideally for maximum flexibility - TO BE FIXED
    protected abstract fun getFullScreenTransitionType(screenData: T, direction: TransitionDirection) : TransitionType.Full

    fun startGenerateNewScreen(context: Context, screenTag: String, type : ScreenType) : Screen<*> {
        val screen = generateNewScreen(context, screenTag, type)
        screen.initialize(type)
        return screen
    }

    protected abstract fun generateNewScreen(context: Context, screenTag: String, type : ScreenType) : Screen<*>

    // A manual copy since sealed/data classes don't have deep copy...
    // SO MAKE SURE EVERY SINGLE PROPERTY IS CREATED AS A NEW OBJECT
    // Otherwise, things break when things actually point to the same object in memory
}