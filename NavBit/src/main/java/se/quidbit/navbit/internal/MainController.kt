package se.quidbit.navbit.internal

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import se.quidbit.navbit.BuildConfig
import se.quidbit.navbit.R
import se.quidbit.navbit.fadeIn
import se.quidbit.navbit.types.InteractionResult
import se.quidbit.navbit.toimplement.NavBitActivity
import se.quidbit.navbit.toimplement.NavBitInteraction
import se.quidbit.navbit.toimplement.NavBitInteractionHandler
import se.quidbit.navbit.toimplement.NavBitNavigationState
import se.quidbit.navbit.toimplement.NavBitNavigationStateHandler
import se.quidbit.navbit.toimplement.NavBitScreenData
import se.quidbit.navbit.toimplement.NavBitScreenHandler
import se.quidbit.navbit.types.NavigationResult
import se.quidbit.navbit.types.ScreenDataResult
import se.quidbit.navbit.types.ScreenType
import se.quidbit.navbit.types.TransitionDirection
import se.quidbit.navbit.toimplement.StartState

internal class MainController<I : NavBitInteraction, S : NavBitNavigationState, D : NavBitScreenData>(
    private val activity: AppCompatActivity,
    private val interactionHandler: NavBitInteractionHandler<I, S>,
    private val stateHandler: NavBitNavigationStateHandler<S, D>,
    private val screenHandler: NavBitScreenHandler<S,D>
) {
    // Set up the container
    // --------------------------------------------------------
    private var mainContainer: FrameLayout

    init {
        val inflater = LayoutInflater.from(activity)

        val base = inflater.inflate(R.layout._nb_base, null) as FrameLayout
        mainContainer = base.findViewById(R.id._nb_main_container)

        // To avoid the background progress bar showing up on normal startup
        // -------------------------------------------------------------------
        val progressBar = base.findViewById<ProgressBar>(R.id._nb_loading)
        progressBar.fadeIn(300, 600)
        // -------------------------------------------------------------------

        activity.setContentView(base)

        ViewCompat.setOnApplyWindowInsetsListener(base) { _, insets ->
            // Pass the insets to each child view
            for (i in 0 until base.childCount) {
                val child = base.getChildAt(i)
                ViewCompat.dispatchApplyWindowInsets(child, insets)
            }
            insets
        }

        // Make sure we have a valid app state before starting
        // --------------------------------------------------------
        when (stateHandler.initialize()) {
            StartState.Restoring -> ScreenController.restoreScreens(mainContainer, screenHandler)
            StartState.New -> {
                // -----------------------------------------------------------------------
                // Go to the start screen
                //  Note: Ideally, this should use the interaction system to reduce this code duplication
                // -----------------------------------------------------------------------
                val (startScreen, screenType) = when (val result = screenHandler.screenDataFromNavigationState(
                    stateHandler.getCurrentState(), activity)) {
                    is ScreenDataResult.ErrorRead -> Pair(stateHandler.fallbackStartupScreenData(activity),
                        ScreenType.Full
                    )
                    is ScreenDataResult.Success -> Pair(result.data, result.type)
                }

                // Open the screen
                //----------------------------------------
                switchScreen(stateHandler.getCurrentState(), startScreen, screenType,
                    TransitionDirection.Forward
                )
            }
        }
    }

    fun onDestroy() {
        ScreenController.destroyScreens(mainContainer)
        activity.setContentView(FrameLayout(activity))
    }

    // --------------------------------------------------------
    // Interaction Handling
    // --------------------------------------------------------

    fun handleInteraction(interaction: I) {
        Log.i("NavBit", "Interaction Received: ${StringHelper.prettyPrintSealedClassString(interaction.toString())} - ${stateHandler.getCurrentState().prettyString()}")

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
                showError("Interaction", "Unexpected: [${StringHelper.prettyPrintSealedClassString(interaction.toString())}]")
            is InteractionResult.ErrorRead ->
                showError("Interaction", "Error Reading: [${interactionResult.error}]")
            is InteractionResult.CloseApp ->
                activity.finish()
            is InteractionResult.NewState -> {
                Log.i("NavBit", " -New State: ${interactionResult.state.prettyString()}")
                stateHandler.setCurrentState(interactionResult.state)

                val screen = ScreenController.getCurrentScreen()
                val screenData = screen.getData<D>()

                when(val navigationResult = stateHandler.getNavigationResult(screenData, activity, screenHandler)) {
                    is NavigationResult.ErrorRead ->
                        showError("Navigation", " -Error Reading [${navigationResult.error}]")
                    is NavigationResult.Navigate -> {
                        Log.i("NavBit", " -Navigating ${interactionResult.direction} to Screen ${
                            StringHelper.prettyPrintSealedClassString(
                                navigationResult.data.toString()
                            )
                        } - ${navigationResult.type}")
                        switchScreen(interactionResult.state, navigationResult.data, navigationResult.type, interactionResult.direction)
                    }
                    is NavigationResult.Update -> {
                        Log.i("NavBit", " -Updating Screen ${
                            StringHelper.prettyPrintSealedClassString(
                                navigationResult.data.toString()
                            )
                        }")
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
                                        is ScreenDataResult.ErrorRead -> return
                                        is ScreenDataResult.Success -> {
                                            // Make sure the data is of a correct type before updating
                                            // The coming screen is not guaranteed to back to the one that was just shown
                                            if (backgroundScreen.getScreenTag() == screenResult.data.tag()) {
                                                backgroundScreen.notifyUpdatedData(screenResult.data)
                                            }
                                        }
                                    }
                                }
                                else -> return
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
        NavBitActivity.mainHandler.post {
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
    private fun switchScreen(state : S, screenData: D, screenType : ScreenType, direction: TransitionDirection) {

        //Start by hiding any visible keyboard as it should not retain between screens
        // ---------------------------------------------------
        val view = activity.findViewById<View>(android.R.id.content)
        if (view != null) {
            val imm = activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        // Go to the new screen
        // ---------------------------------------------------
        NavBitActivity.backgroundHandler.post {
            ScreenController.goToScreen(
                activity,
                mainContainer,
                screenData,
                screenType,
                direction,
                screenHandler
            )
            ScreenController.cleanupScreens(activity.lifecycleScope, mainContainer)

            stateHandler.getScreenToPreload(state)?.let { (data, screenType) ->
                ScreenController.preInitializeScreen(activity, screenHandler, data, screenType)
            }
        }
    }
}