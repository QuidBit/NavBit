package se.quidbit.navbit

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

abstract class NavBitNavigationStateHandler <T : NavBitNavigationState, U : NavBitScreenData> {
    private lateinit var state: T

    fun setCurrentState(state : T) {
        this.state = state
        onSettingState(state)
    }

    fun initialize() : StartState {
        // We should only perform an initialization if a state is not set (on app startup)
        // Not after a screen rotation, in which everything is set up again as well
        return if (!this::state.isInitialized) {
            val startState = prepareStartup()
            onSettingState(startState)
            setCurrentState(startState)
            StartState.New
        } else {
            StartState.Restoring
        }
    }

    fun getCurrentState() : T {
        // Dangerous cast here!
        // However, it is safe as long as this function is only called with the same type as was used to find the screen
        // Making them guaranteed to match, so it has to be enforced within the library
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
    abstract fun prepareStartup() : T
    abstract fun onSettingState(state : T)
}

enum class StartState {
    New,
    Restoring
}