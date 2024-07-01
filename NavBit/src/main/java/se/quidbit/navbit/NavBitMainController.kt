package se.quidbit.navbit

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope

class NavBitMainController<T : NavBitInteraction, U : NavBitNavigationState, V : NavBitScreenData>(
    private val activity: AppCompatActivity,
    private val interactionHandler: NavBitInteractionHandler<T,U>,
    private val stateHandler: NavBitNavigationStateHandler<U, V>,
    private val screenHandler: NavBitScreenHandler<V>
) {
    private var mainContainer : FrameLayout
    private var currentScreen : Pair<Screen<*>, V>? = null

    init {
        // Prepare starting state
        // ----------------------------------
        val startState = stateHandler.initializeAndGetStartState()
        stateHandler.onSettingState(startState)
        stateHandler.setCurrentState(startState)

        // Set up the container
        // --------------------------------------------------------
        mainContainer = FrameLayout(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundResource(R.color.container_background)
        }
        activity.setContentView(mainContainer)

        ViewCompat.setOnApplyWindowInsetsListener(mainContainer) { _, insets ->
            // Pass the insets to each child view
            for (i in 0 until mainContainer.childCount) {
                val child = mainContainer.getChildAt(i)
                ViewCompat.dispatchApplyWindowInsets(child, insets)
            }
            insets
        }

        // Restore screens (after activity recreation, for example after rotation)
        // --------------------------------------------------------
        ScreenController.restoreScreens(mainContainer, screenHandler)

        // -----------------------------------------------------------------------
        // Go to the start screen
        //  Note: Ideally, this should use the interaction system to reduce this code duplication
        // -----------------------------------------------------------------------
        val (startFragment, screenType) = when (val result = screenHandler.screenDataFromNavigationState(
            stateHandler.getCurrentState(), activity)) {
            is ScreenDataResult.ErrorRead -> Pair(stateHandler.fallbackStartScreenData(activity), ScreenType.Full)
            is ScreenDataResult.Success -> Pair(result.data, result.type)
        }

        // Open the fragment
        //----------------------------------------
        switchScreen(startFragment, screenType, TransitionDirection.Forward)
    }

    fun onDestroy() {
        ScreenController.destroyScreens(mainContainer)
    }

    // --------------------------------------------------------
    // Interaction Handling
    // --------------------------------------------------------

    fun handleInteraction(interaction: T) {
        Log.i("NavBit", "Interaction Received: ${StringHelper.prettyPrintSealed(interaction.toString())} - ${stateHandler.getCurrentState().prettyString()}")

        // Handle the interactions
        // ---------------------------------------------------------------------------------
        val interactionResult = interactionHandler.applyInteractionOnState(
            interaction,
            stateHandler.getCurrentState(),
            activity
        )

        when(interactionResult) {
            is InteractionResult.Ignore -> {
                // Used when the view should not be updated by an interaction,
                // for example ignoring no longer relevant API calls coming in
            }
            is InteractionResult.Unexpected ->
                showError("Interaction", "Unexpected: [${StringHelper.prettyPrintSealed(interaction.toString())}]")
            is InteractionResult.ErrorRead ->
                showError("Interaction", "Error Reading: [${interactionResult.error}]")
            is InteractionResult.CloseApp ->
                activity.finish()
            is InteractionResult.NewState -> {
                Log.i("NavBit", "New State: ${interactionResult.state.prettyString()}}")
                stateHandler.setCurrentState(interactionResult.state)

                currentScreen?.let { (screen, screenData) ->
                    when(val navigationResult = stateHandler.getNavigationResult<V>(screenData, activity, screenHandler)) {
                        is NavigationResult.ErrorRead ->
                            showError("Navigation", "Error Reading [${navigationResult.error}]")
                        is NavigationResult.Navigate -> {
                            Log.i("NavBit", "Navigating ${interactionResult.direction} to Screen ${StringHelper.prettyPrintSealed(navigationResult.data.toString())} - ${navigationResult.type}")
                            switchScreen(navigationResult.data, navigationResult.type, interactionResult.direction)
                        }
                        is NavigationResult.Update -> {
                            Log.i("NavBit", "Updating Screen ${StringHelper.prettyPrintSealed(navigationResult.data.toString())}}")
                            screen.notifyUpdatedData(navigationResult.data)

                            // Also update any visible screens behind the current one (sheet support)
                            // ---------------------------------------------------------------------------
                            var backState = stateHandler.getCurrentState()

                            for (backgroundScreen in ScreenController.getBackgroundScreens()) {
                                // Simply update them with the data that would be generated if we backed to that screen
                                when(val backResult = interactionHandler.applyBackInteractionOnState(backState)) {
                                    is InteractionResult.NewState -> {
                                        backState = backResult.state
                                        when (val screenResult = screenHandler.screenDataFromNavigationState(backState, activity)) {
                                            is ScreenDataResult.ErrorRead -> return@let
                                            is ScreenDataResult.Success -> {
                                                // Make sure the data is of a correct type before updating
                                                // The coming screen is not guaranteed to back to the one that was just shown
                                                if (backgroundScreen.getScreenTag() == screenResult.data.tag()) {
                                                    backgroundScreen.notifyUpdatedData(screenResult.data)
                                                }
                                            }
                                        }
                                    }
                                    else -> return@let
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showError(source : String, error : String) {
        val currentState = stateHandler.getCurrentState()
        val infoString = "$error - ${currentState.prettyString()}"

        Log.e("NavBit", "$source $infoString")

        // If we are debugging, we can show the error directly on screen for simplicity
        Handler(Looper.getMainLooper()).post {
            if (BuildConfig.DEBUG) {
                Toast.makeText(
                    activity,
                    "$source - $infoString",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // --------------------------------------------------------
    // Screen Handling
    // --------------------------------------------------------
    private fun switchScreen(screenData: V, screenType : ScreenType, direction: TransitionDirection) {
        //Start by hiding any visible keyboard as it should not retain between screens
        // ---------------------------------------------------
        val view = activity.findViewById<View>(android.R.id.content)
        if (view != null) {
            val imm = activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        // Get the new screen
        // ---------------------------------------------------
        Handler(Looper.getMainLooper()).post{
            val newScreen = ScreenController.goToScreen(activity, mainContainer, screenData, screenType, direction, screenHandler)
            currentScreen = Pair(newScreen, screenData)
            ScreenController.cleanupScreens(activity.lifecycleScope, mainContainer)
        }
    }
}