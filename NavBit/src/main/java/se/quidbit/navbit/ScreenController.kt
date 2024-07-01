package se.quidbit.navbit

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ScreenController {
    private val backstack   = ArrayList<Pair<Screen<*>, ScreenType>>()
    private val toBeRemoved = ArrayList<Pair<Screen<*>, ScreenType>>()

    fun <T : NavBitScreenData> goToScreen(
        context : Context,
        mainContainer : FrameLayout,
        screenData : T,
        type : ScreenType,
        direction: TransitionDirection,
        screenHandler: NavBitScreenHandler<T>
    ) : Screen<*> {
        val result = when (direction) {
            TransitionDirection.Forward -> {

                // Animate away the current screen
                // -----------------------------------------------------------
                val transition = screenHandler.getScreenTransition(screenData, type, direction)

                backstack.lastOrNull()?.let { (oldScreen, _) ->
                    Handler(Looper.getMainLooper()).post {
                        oldScreen.initiateLeavingTransition(transition)
                    }
                }

                // Add the new screen
                // ----------------------------------------------------------
                val screen = addNewScreen(context, mainContainer, screenData, type, screenHandler)

                goToResult(screen, transition, true)
            }
            TransitionDirection.Backward -> {
                if (!backStackContains<T>(screenData.tag(), type)) {
                    // We are backing to a screen that is not available

                    // Take the current screen
                    // ------------------------------------------------------------------
                    val old = backstack.removeAt(backstack.lastIndex)
                    val (oldScreen, oldType) = old

                    // Add a new screen at the end of the stack instead
                    // ------------------------------------------------------------------
                    val screen = addNewScreen(context, mainContainer, screenData, type, screenHandler)

                    // Remove and re-add the leaving screen to make sure it visually stays on top
                    // ------------------------------------------------------------------
                    mainContainer.removeView(oldScreen)
                    mainContainer.addView(oldScreen)

                    // Animate out the current screen
                    // ------------------------------------------------------------------
                    val transition = screenHandler.getScreenTransition(
                        oldScreen.getData(),
                        oldType,
                        direction
                    )

                    oldScreen.initiateLeavingTransition(transition)
                    toBeRemoved.add(old)

                    goToResult(screen, transition, true)
                } else {
                    // Search the backstack in reverse order for the screen we are backing to

                    // Check how the current screen should be animated out
                    // ----------------------------------------------------------
                    val transition = backstack.lastOrNull()?.let {
                        screenHandler.getScreenTransition(it.first.getData(), it.second, direction)
                    } ?: run {
                        ScreenTransition(TransitionType.Full.Slide, direction)
                    }

                    // Remove until we find the screen we are looking for
                    var foundScreen : Screen<*>? = null

                    val toRemove = mutableListOf<Pair<Screen<*>, ScreenType>>()

                    backstack.takeLastWhile { screen ->
                        if (screen.first.getData<T>().tag() == screenData.tag()) {
                            foundScreen = screen.first
                        } else {
                            toRemove.add(screen)
                        }

                        foundScreen == null
                    }

                    // Make sure all that is getting dropped from the backstack is also animated away
                    for (toRemoveScreen in toRemove) {
                        Handler(Looper.getMainLooper()).post {
                            toRemoveScreen.first.initiateLeavingTransition(transition)
                        }
                    }

                    backstack.removeAll(toRemove.toSet())
                    toBeRemoved.addAll(toRemove)

                    // NOTE! HAVING TO ADD A NEW SCREEN HERE SHOULD NEVER HAPPEN
                    // Since we check backstack contains first, but lets include it for completeness/avoid crashing
                    var newlyAdded = false
                    val screen = foundScreen ?: run {
                        newlyAdded = true
                        addNewScreen(context, mainContainer, screenData, type, screenHandler)
                    }

                    goToResult(screen, transition, newlyAdded)
                }
            }
        }

        // Animate in the screen transition (must be done on the main thread)
        // ---------------------------------------------------
        Handler(Looper.getMainLooper()).post {
            result.screen.initiateAppearingTransition(result.transition, screenData, result.newlyAdded)
        }

        return result.screen
    }

    private fun <T : NavBitScreenData> addNewScreen(context: Context, mainContainer: FrameLayout, screenData : T, type : ScreenType, screenHandler : NavBitScreenHandler<T>) : Screen<*> {
        val screen = screenHandler.startGenerateNewScreen(context, screenData, type)
        backstack.add(Pair(screen, type))

        // Make sure the screen appears with the correct insets
        ViewCompat.getRootWindowInsets(mainContainer)?.let { startInsets ->
            ViewCompat.dispatchApplyWindowInsets(screen, startInsets)
        }

        // Then add it for display
        mainContainer.addView(screen)

        return screen
    }

    private fun <T : NavBitScreenData> backStackContains(screenTag : String, type : ScreenType) : Boolean {
        for (screen in backstack) {
            if (screen.first.getData<T>().tag() == screenTag && screen.second == type) {
                return true
            }
        }
        return false
    }

    fun cleanupScreens(lifecycleScope : LifecycleCoroutineScope, mainContainer : FrameLayout) {
        // Using a delay to spread out the work over time to reduce risk of lag
        val timeSpacing = 100L

        lifecycleScope.launch(Dispatchers.IO) {
            var totalDelay = 0L

            delay(ScreenTransition.TRANSITION_LENGTH + timeSpacing)

            // Clear all toBeRemoved that are no longer transitioning
            // ------------------------------------------------------------
            toBeRemoved.removeAll { screen ->
                val remove = screen.first.visibility == View.INVISIBLE
                if (remove) {
                    totalDelay += timeSpacing

                    Handler(Looper.getMainLooper()).postDelayed({
                        mainContainer.removeView(screen.first)
                    }, totalDelay)
                }

                remove
            }

            delay(totalDelay)
            Log.i("NavBit", "Screens cleanup done - toBeRemoved: ${toBeRemoved.size}")
        }
    }

    fun getBackgroundScreens() : ArrayList<Screen<*>> {
        val result = ArrayList<Screen<*>>()
        var first = true

        backstack.takeLastWhile { screen ->
            // Skip the first/current screen (we are interested in those behind it)
            if (!first) {
                if (screen.first.isBackgroundScreen()) {
                    result.add(screen.first)

                    // Continue looking for deeper nesting
                    true
                } else {
                    // The first time a non-background screen is found, we can stop looking
                        // As nothing behind it can be visible in that case
                    false
                }
            } else {
                first = false
                true
            }
        }

        return result
    }

    // -------------------------------------------------------------
    // Destroying/restoring screens
    // -------------------------------------------------------------
    // Used on startup/rotation to make sure all old views are properly recreated

    fun destroyScreens(container : FrameLayout) {
        // To be removed
        // -------------------------------
        for (screen in toBeRemoved) {
            container.removeView(screen.first)
        }
        toBeRemoved.clear()

        // Backstack
        // -------------------------------
        for (screen in backstack) {
            container.removeView(screen.first)
        }
        // NOTE: The backstack is NOT emptied, since the data will be reused during restoring
            // With the exception being the last/current screen
            // Which is removed to make sure that no duplicate is generated
            // As the current screen is always generated during startup
        backstack.removeLastOrNull()
    }

    fun <T : NavBitScreenData>restoreScreens(container : FrameLayout, screenGenerator: NavBitScreenHandler<T>) {

        // Take a copy of all
        // -------------------------------
        val oldStack =  ArrayList<Pair<Screen<*>, ScreenType>>()

        for (screen in backstack) {
            oldStack.add(screen)
        }
        backstack.clear()

        // Regenerate
        // -------------------------------
        for ((oldScreen, screenType) in oldStack) {
            val oldData = oldScreen.getData() as T
            val newScreen = screenGenerator.startGenerateNewScreen(container.context, oldData, screenType)
            val visible = oldScreen.visibility == View.VISIBLE

            Handler(Looper.getMainLooper()).post {
                newScreen.restoreScreen(oldData, visible)
            }

            backstack.add(Pair(newScreen, screenType))
            container.addView(newScreen)
        }
    }
}

data class goToResult (
    val screen : Screen<*>,
    val transition: ScreenTransition,
    val newlyAdded : Boolean
)