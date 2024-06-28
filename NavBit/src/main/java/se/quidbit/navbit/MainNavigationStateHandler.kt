package se.quidbit.navbit

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

abstract class NavBitNavigationStateHandler <T : NavBitNavigationState, U : NavBitScreenData> {
    private lateinit var state: T

    fun setCurrentState(state : T) {
        this.state = state
        onSettingState(state)
    }

    fun getCurrentState() : T {
        return state.deepCopy() as T
    }

    fun <T : NavBitScreenData>getNavigationResult(oldScreenData: U, baseActivity: AppCompatActivity, screenGenerator : NavBitScreenHandler<U>) : NavigationResult<U> {

        // Check if we are navigating elsewhere, or simply updating a current fragment
        val (newScreenData, newScreenType) = when (val screenResult =
            screenGenerator.screenDataFromNavigationState(
                state,
                baseActivity
            )) {
            is ScreenDataResult.ErrorRead -> return NavigationResult.ErrorRead(
                screenResult.error
            )
            is ScreenDataResult.Success -> screenResult
        }

        // Return result
        return if (oldScreenData.javaClass == newScreenData.javaClass) {
            NavigationResult.Update(newScreenData)
        } else {
            // Moving to a fully new state
            //AnalyticsHandler.getMain(baseActivity).trackNavigateState()

            NavigationResult.Navigate(
                newScreenType,
                newScreenData
            )
        }
    }

    // -------------------------------------------------------------------------------

    abstract fun fallbackStartScreenData(context: Context) : U
    abstract fun initializeAndGetStartState() : T
    abstract fun onSettingState(state : T)
}