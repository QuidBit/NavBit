package se.quidbit.navbit.toimplement

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import se.quidbit.navbit.types.NavigationResult
import se.quidbit.navbit.types.ScreenDataResult
import se.quidbit.navbit.types.ScreenType

abstract class NavBitNavigationStateHandler <S : NavBitNavigationState, D : NavBitScreenData> {
    private lateinit var state: S

    fun setCurrentState(state : S) {
        val newState = if (!this::state.isInitialized) {
            true
        } else {
            state.javaClass != this.state.javaClass
        }

        this.state = state
        storeStartupNavigationState(state)
        if (newState) {
            onNavigatingToNewState(state)
        }
    }

    fun initialize() : StartState {
        // We should only perform an initialization if a state is not set (on app startup)
        // Not after a screen rotation, in which everything is set up again as well
        return if (!this::state.isInitialized) {
            val startState = loadStartupNavigationState()
            storeStartupNavigationState(startState)
            setCurrentState(startState)
            StartState.New
        } else {
            StartState.Restoring
        }
    }

    fun getCurrentState() : S {
        return state
    }

    fun getNavigationResult(oldScreenData: D, baseActivity: AppCompatActivity, screenGenerator : NavBitScreenHandler<S, D>) : NavigationResult<D> {

        // Check if we are navigating elsewhere, or simply updating a current screen
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

    abstract fun fallbackStartupScreenData(context: Context) : D
    abstract fun loadStartupNavigationState() : S
    abstract fun storeStartupNavigationState(state : S)
    abstract fun onNavigatingToNewState(state : S)

    abstract fun getScreenToPreload(state : S) : Pair<D, ScreenType>?
}

enum class StartState {
    New,
    Restoring
}