package se.quidbit.navbit

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class ScreenPreInitializedState {
    class InProgress(val onDone : ((Screen<*>) -> Unit)?) : ScreenPreInitializedState()
    class Done(val screen : Screen<*>) : ScreenPreInitializedState()
}
object ScreenController {
    private val backstack   = ArrayList<Pair<Screen<*>, ScreenType>>()
    private val toBeRemoved = ArrayList<Pair<Screen<*>, ScreenType>>()

    private val toBeRestored = ArrayList<Pair<NavBitScreenData, ScreenType>>()

    private val preInitializedScreens = HashMap<String, ScreenPreInitializedState>()

    private fun preInitId(data : NavBitScreenData, type : ScreenType) : String {
        return "${data.tag()}${type}"
    }

    fun <T : NavBitScreenData> preInitializeScreen(
        context : Context,
        screenHandler: NavBitScreenHandler<T>,
        screenData : NavBitScreenData,
        type : ScreenType,
    ) {
        val id = preInitId(screenData, type)
        Log.e("ScreenController", "[$id] - Pre-initialize")

        // Check so it has not already been requested
        if (preInitializedScreens.containsKey(id)) {
            return
        }

        // It has not, so start!
        preInitializedScreens[id] = ScreenPreInitializedState.InProgress(null)
        screenHandler.startGenerateNewScreen(context, screenData as T, type) { screen ->
            Log.e("ScreenController", "[$id] - Pre-initialize --READY")

            preInitializedScreens[id]?.let { state ->
                when (state) {
                    is ScreenPreInitializedState.InProgress -> {
                        if (state.onDone != null) {
                            Log.e("ScreenController", "[$id] - Pre-initialize----Use directly")
                            state.onDone.invoke(screen)
                            preInitializedScreens.remove(screenData.tag())
                        } else {
                            // Store for later use
                            Log.e("ScreenController", "[$id] - Pre-initialize ----Store for later")
                            preInitializedScreens[id] = ScreenPreInitializedState.Done(screen)
                        }
                    }
                    is ScreenPreInitializedState.Done -> {}
                }
            }
        }
    }

    fun <T : NavBitScreenData> goToScreen(
        context : Context,
        mainContainer : FrameLayout,
        screenData : T,
        type : ScreenType,
        direction: TransitionDirection,
        screenHandler: NavBitScreenHandler<T>
    ) {
        when (direction) {
            TransitionDirection.Forward -> {

                // Animate away the current screen
                // -----------------------------------------------------------
                val transition = screenHandler.getScreenTransition(screenData, type, direction)

                backstack.lastOrNull()?.let { (oldScreen, _) ->
                    NavBitActivity.mainHandler.post {
                        oldScreen.initiateLeavingTransition(transition)
                    }
                }

                // Add the new screen
                // ----------------------------------------------------------
                // Check if it is available among the pre-initialized
                val id = preInitId(screenData, type)

                preInitializedScreens[id]?.let { state ->
                    when (state) {
                        // It is done, so use it now
                        is ScreenPreInitializedState.Done -> {
                            Log.e("ScreenController", "[$id] - GoTo --- DIRECTLY")
                            backstack.add(Pair(state.screen, type))
                            completeGoToScreen(state.screen, transition, true, mainContainer, screenData)
                            preInitializedScreens.remove(id)
                        }
                        // Wait for its completion
                        is ScreenPreInitializedState.InProgress -> {
                            Log.e("ScreenController", "[$id] - GoTo --- Wait for ready...")
                            preInitializedScreens[id] = ScreenPreInitializedState.InProgress {
                                backstack.add(Pair(it, type))
                                completeGoToScreen(it, transition, true, mainContainer, screenData)
                                preInitializedScreens.remove(id)
                            }
                        }
                    }
                }
                // Not available, so generate a new screen
                ?: run {
                    Log.e("ScreenController", "[$id] - GoTo --- Generate")
                    addNewScreen(context, screenData, type, screenHandler) {
                        completeGoToScreen(it, transition, true, mainContainer, screenData)
                    }
                }
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
                    addNewScreen(context, screenData, type, screenHandler) {
                        // Remove and re-add the leaving screen to make sure it visually stays on top
                        // ------------------------------------------------------------------
                        NavBitActivity.mainHandler.post {
                            mainContainer.removeView(oldScreen)
                            mainContainer.addView(oldScreen)
                        }

                        // Animate out the current screen
                        // ------------------------------------------------------------------
                        val transition = screenHandler.getScreenTransition(
                            oldScreen.getData(),
                            oldType,
                            direction
                        )

                        NavBitActivity.mainHandler.post {
                            oldScreen.initiateLeavingTransition(transition)
                        }
                        toBeRemoved.add(old)

                        completeGoToScreen(it, transition, true, mainContainer, screenData)
                    }
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
                        NavBitActivity.mainHandler.post {
                            toRemoveScreen.first.initiateLeavingTransition(transition)
                        }
                    }

                    backstack.removeAll(toRemove.toSet())
                    toBeRemoved.addAll(toRemove)

                    // NOTE! HAVING TO ADD A NEW SCREEN HERE SHOULD NEVER HAPPEN
                    // Since we check backstack contains first, but lets include it for completeness/avoid crashing

                    foundScreen?.let {
                        completeGoToScreen(it, transition, false, mainContainer, screenData)
                    } ?: run {
                        addNewScreen(context , screenData, type, screenHandler) {
                            completeGoToScreen(it, transition, true, mainContainer, screenData)
                        }
                    }
                }
            }
        }
    }

    private fun <T : NavBitScreenData> completeGoToScreen(screen : Screen<*>, transition: ScreenTransition, newlyAdded : Boolean, mainContainer: FrameLayout, screenData: T) {
        screen.initiateAppearingTransition(transition, screenData, newlyAdded) {

            if (newlyAdded) {
                //--------------------------------------------------------------------
                // DANGER ZONE
                //--------------------------------------------------------------------
                // Moving the generated view to a container owned by main thread for display on screen
                // Gives great performance with most work off the main thread
                //
                //    BUT IT IS NOT OFFICIALLY SUPPORTED BY ANDROID
                //
                // To be removed if it causes issues, going back to doing all with the views on the main thread
                //--------------------------------------------------------------------

                NavBitActivity.mainHandler.post {
                    // Make sure the screen appears with the correct insets
                    ViewCompat.getRootWindowInsets(mainContainer)?.let { startInsets ->
                        ViewCompat.dispatchApplyWindowInsets(screen, startInsets)
                    }

                    // Then add it for display
                    mainContainer.addView(screen)
                }
            }
        }
    }

    private fun <T : NavBitScreenData> addNewScreen(context: Context , screenData : T, type : ScreenType, screenHandler : NavBitScreenHandler<T>, onAdded : (Screen<*>) -> Unit) {
        screenHandler.startGenerateNewScreen(context, screenData, type) { screen ->
            backstack.add(Pair(screen, type))
            onAdded(screen)
        }
    }

    private fun <T : NavBitScreenData> backStackContains(screenTag : String, type : ScreenType) : Boolean {
        for (screen in backstack) {
            if (screen.first.getData<T>().tag() == screenTag && screen.second == type) {
                return true
            }
        }
        return false
    }

    fun getCurrentScreen() : Screen<*> {
        return backstack[backstack.lastIndex].first
    }

    fun cleanupScreens(lifecycleScope : LifecycleCoroutineScope, mainContainer : FrameLayout) {
        // Using a delay to spread out the work over time to reduce risk of lag
        val timeSpacing = 100L

        lifecycleScope.launch(Dispatchers.IO) {
            var totalDelay = 0L

            delay(BASE_TRANSITION_LENGTH.toLong() + timeSpacing)

            // Clear all toBeRemoved that are no longer transitioning
            // ------------------------------------------------------------
            toBeRemoved.removeAll { screen ->
                val remove = screen.first.visibility == View.INVISIBLE
                if (remove) {
                    totalDelay += timeSpacing

                    NavBitActivity.mainHandler.postDelayed({
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
        // Remove all to be removed
        // -------------------------------
        for (screen in toBeRemoved) {
            container.removeView(screen.first)
        }
        toBeRemoved.clear()

        // Remove all in the backstack
        // -------------------------------
        for (screen in backstack) {
            container.removeView(screen.first)
        }

        // Copy the relevant data for restoring later
        toBeRestored.clear()
        for (screen in backstack.reversed()) {
            toBeRestored.add(Pair(screen.first.getData(), screen.second))

            // Skip restoring screens that are hidden (i.e., the display is already covered by a screen of "full" type)
            if (screen.second == ScreenType.Full) {
                break
            }
        }
        backstack.clear()
    }

    fun <T : NavBitScreenData>restoreScreens(container : FrameLayout, screenHandler: NavBitScreenHandler<T>) {
        Log.i("NavBit", "Restoring ${toBeRestored.size} screens")

        for ((oldData, screenType) in toBeRestored.reversed()) {
            Log.i("NavBit", "    --- $oldData - $screenType")

            screenHandler.startGenerateNewScreen(container.context, oldData as T, screenType) { newScreen ->
                NavBitActivity.mainHandler.post {
                    newScreen.restoreScreen(oldData)
                }

                backstack.add(Pair(newScreen, screenType))
                container.addView(newScreen)
            }
        }

        toBeRestored.clear()
    }
}