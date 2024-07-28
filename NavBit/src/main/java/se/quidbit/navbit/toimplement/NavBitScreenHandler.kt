package se.quidbit.navbit.toimplement

import android.content.Context
import se.quidbit.navbit.types.TransitionDirection
import se.quidbit.navbit.types.TransitionType
import se.quidbit.navbit.types.ScreenDataResult
import se.quidbit.navbit.internal.ScreenTransition
import se.quidbit.navbit.types.ScreenType

abstract class NavBitScreenHandler<T : NavBitScreenData> {
    abstract fun screenDataDeepCopy(t : NavBitScreenData) : T
    abstract fun screenDataFromNavigationState(s: NavBitNavigationState, context : Context) : ScreenDataResult<T>


    internal fun getScreenTransition(screenData: T, screenType : ScreenType, direction: TransitionDirection) : ScreenTransition {
        val transitionType = when (screenType) {
            ScreenType.Sheet -> TransitionType.Sheet
            ScreenType.PopUp -> TransitionType.PopUp
            else -> getFullScreenTransitionType(screenData, direction)
        }
        return ScreenTransition(transitionType, direction)
    }

    // NOTE: Should be based on NavigationState instead of screen data ideally for maximum flexibility - TO BE FIXED
    protected abstract fun getFullScreenTransitionType(screenData: T, direction: TransitionDirection) : TransitionType.Full

    internal fun startGenerateNewScreen(context: Context, screenData: T, type : ScreenType, onGenerated : (NavBitScreen<*>) -> Unit) {
        val screen = generateNewScreen(context, screenData, type)
        screen.initialize(type) {
            onGenerated(screen)
        }
    }

    protected abstract fun generateNewScreen(context: Context, screenData: T, type : ScreenType) : NavBitScreen<*>

    // A manual copy since sealed/data classes don't have deep copy...
    // SO MAKE SURE EVERY SINGLE PROPERTY IS CREATED AS A NEW OBJECT
    // Otherwise, things break when things actually point to the same object in memory
}