package se.quidbit.navbit

import android.content.Context

abstract class NavBitScreenHandler<T : NavBitScreenData> {
    abstract fun screenDataDeepCopy(t : NavBitScreenData) : T
    abstract fun screenDataFromNavigationState(s: NavBitNavigationState, context : Context) : ScreenDataResult<T>

    // NOTE: Should be based on NavigationState instead of screen data ideally for maximum flexibility - TO BE FIXED
    abstract fun getTransitionType(fragment: T, screenType : ScreenType, direction: TransitionDirection) : TransitionType

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