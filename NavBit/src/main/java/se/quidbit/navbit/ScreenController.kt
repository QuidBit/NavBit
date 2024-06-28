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
        val (newScreen, transition) = when (direction) {
            TransitionDirection.Forward -> {
                // Animate away the current screen
                // -----------------------------------------------------------
                val transition = ScreenTransition(
                    screenHandler.getTransitionType(screenData, type, direction),
                    direction
                )

                backstack.lastOrNull()?.let { (oldScreen, _) ->
                    Handler(Looper.getMainLooper()).post {
                        oldScreen.initiateLeavingTransition(transition)
                    }
                }

                // Add the new screen
                // ----------------------------------------------------------
                val screen = screenHandler. startGenerateNewScreen(context, screenData.tag(), type)
                backstack.add(Pair(screen, type))

                // Make sure the screen appears with the correct insets
                ViewCompat.getRootWindowInsets(mainContainer)?.let { startInsets ->
                    ViewCompat.dispatchApplyWindowInsets(screen, startInsets)
                }

                // Then add it for display
                mainContainer.addView(screen)

                Pair(screen, transition)
            }
            TransitionDirection.Backward -> {
                val transitionType = backstack.lastOrNull()?.let {
                    screenHandler.getTransitionType(it.first.getData() as T, it.second, direction)
                } ?: run {
                    TransitionType.Slide
                }
                val transition = ScreenTransition(transitionType, direction)

                // Search the backstack in reverse order for the screen we are backing to
                var foundScreen : Screen<*>? = null

                val toRemove = mutableListOf<Pair<Screen<*>, ScreenType>>()

                backstack.takeLastWhile { screen ->
                    if (screen.first.getData().tag() == screenData.tag()) {
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

                val finalScreen = foundScreen ?: run {
                    Log.e("ScreenCacher", "getScreen() - Backing to a view that is not in the backing stack")
                    screenHandler.startGenerateNewScreen(context, screenData.tag(), ScreenType.Full)
                }

                Pair(finalScreen, transition)
            }
        }

        // Animate in the screen transition (must be done on the main thread)
        // ---------------------------------------------------
        Handler(Looper.getMainLooper()).post {
            newScreen.initiateEnteringTransition(transition, screenData)
        }

        return newScreen
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
            Log.i("ScreenCacher", "cleanupAndPreloadScreens() is DONE - toBeRemoved: ${toBeRemoved.size}")
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
            val oldData = oldScreen.getData()
            val newScreen = screenGenerator.startGenerateNewScreen(container.context, oldData.tag(), screenType)
            val visible = oldScreen.visibility == View.VISIBLE

            Handler(Looper.getMainLooper()).post {
                newScreen.restoreScreen(oldData, visible)
            }

            backstack.add(Pair(newScreen, screenType))
            container.addView(newScreen)
        }
    }
}