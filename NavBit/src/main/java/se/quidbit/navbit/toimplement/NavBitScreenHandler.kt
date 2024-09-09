package se.quidbit.navbit.toimplement

import android.content.Context
import se.quidbit.navbit.types.TransitionDirection
import se.quidbit.navbit.types.TransitionType
import se.quidbit.navbit.types.ScreenDataResult
import se.quidbit.navbit.internal.ScreenTransition
import se.quidbit.navbit.types.ScreenType

abstract class NavBitScreenHandler<S : NavBitNavigationState, D : NavBitScreenData> {

    abstract fun screenDataFromNavigationState(s : S, context : Context) : ScreenDataResult<D>

    internal fun getScreenTransition(screenData: D, screenType : ScreenType, direction: TransitionDirection) : ScreenTransition {
        val transitionType = when (screenType) {
            ScreenType.Sheet -> TransitionType.Sheet
            ScreenType.PopUp -> TransitionType.PopUp
            else -> getFullScreenTransitionType(screenData, direction)
        }
        return ScreenTransition(transitionType, direction)
    }

    // NOTE: Should be based on NavigationState instead of screen data ideally for maximum flexibility - TO BE FIXED
    protected abstract fun getFullScreenTransitionType(screenData: D, direction: TransitionDirection) : TransitionType.Full

    internal fun startGenerateNewScreen(context: Context, screenData: D, type : ScreenType, onGenerated : (NavBitScreen<*>) -> Unit) {
        val screen = generateNewScreen(context, screenData, type)
        screen.initialize(type) {
            onGenerated(screen)
        }
    }

    protected abstract fun generateNewScreen(context: Context, screenData: D, type : ScreenType) : NavBitScreen<*>


}