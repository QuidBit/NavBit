package se.quidbit.navbit.internal

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.quidbit.navbit.BuildConfig
import se.quidbit.navbit.toimplement.NavBitInteraction
import se.quidbit.navbit.toimplement.NavBitNavigationState
import se.quidbit.navbit.types.InteractionResult
import se.quidbit.navbit.toimplement.NavBitInteractionHandler
import se.quidbit.navbit.toimplement.NavBitNavigationStateHandler
import se.quidbit.navbit.types.QueuedInteraction

// NOTE
// --------------------------------------------------------------------------------------------------
// The MasterController, which handles all interactions and updates the states
// extends ViewModel, to make sure everything is retained between recreations (such as rotations
// --------------------------------------------------------------------------------------------------

internal class MasterControllerFactory<I : NavBitInteraction, S : NavBitNavigationState> (
    private val interactionHandler: NavBitInteractionHandler<I, S>,
    private val navigationStateHandler: NavBitNavigationStateHandler<S>
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = MasterController(interactionHandler, navigationStateHandler) as T
}

// --------------------------------------------------------------------------------------------------

internal class MasterController<I : NavBitInteraction, S : NavBitNavigationState> (
    private val interactionHandler : NavBitInteractionHandler<I, S>,
    private val navigationStateHandler: NavBitNavigationStateHandler<S>
) : ViewModel() {

    private val _navState = MutableStateFlow(NavigationStates(0, null, navigationStateHandler.loadStartupNavigationState()))
    val  navState: StateFlow<NavigationStates<S>> = _navState

    private var interactionCounter = 0

    fun processInteraction(activity: ComponentActivity, interaction : QueuedInteraction<I>) {
        InfoLog.i( "--[$interactionCounter]-Interaction Processing: $interaction")
        val currentState = _navState.value.current

        val interactionResult = when(interaction) {
            is QueuedInteraction.Back -> interactionHandler.applyBackInteractionOnState(currentState)
            is QueuedInteraction.Close -> interactionHandler.applyCloseInteractionOnState(currentState)
            is QueuedInteraction.Custom -> interactionHandler.applyInteractionOnState(interaction.interaction, currentState)
        }

        when (interactionResult) {
            is InteractionResult.Ignore -> InfoLog.i( "----[$interactionCounter]-Interaction Ignored")
            is InteractionResult.ToDo ->
                showError(activity, currentState,"[$interactionCounter]-Interaction - Not Yet Implemented: [${ StringHelper.prettyPrintSealedClassString(interaction.toString())}", true)
            is InteractionResult.Unexpected ->
                showError(activity, currentState,"[$interactionCounter]-Interaction - Unexpected: [${ StringHelper.prettyPrintSealedClassString(interaction.toString())}", false)
            is InteractionResult.ErrorRead ->
                showError(activity, currentState, "[$interactionCounter]-Interaction - Error Reading: [${interactionResult.error}]", true)
            is InteractionResult.CloseApp ->
                activity.finish()
            is InteractionResult.Complete -> {
                val state = interactionResult.state
                navigationStateHandler.onNavigatingToNewState(state)
                InfoLog.i( "----[$interactionCounter]-Interaction Complete -> State: $state")
                _navState.value = NavigationStates(_navState.value.triggerCounter + 1, _navState.value.current, state)
            }
        }

        interactionCounter += 1
    }

    private fun showError(context: Context, currentState : S, error : String, showToast : Boolean) {
        val infoString = "$error - ${currentState.prettyString()}"

        InfoLog.e("----[$interactionCounter]-Interaction $infoString")

        // If we are debugging, we can show the error directly on screen for simplicity
        if (InfoLog.LOGGING_ENABLED && showToast && interactionHandler.showDebugToasts()) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    context,
                    "[$interactionCounter]-Interaction - $infoString",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun goToFallbackState() {
        val fallbackState = navigationStateHandler.getFallbackState(_navState.value.current)
        navigationStateHandler.onNavigatingToNewState(fallbackState)
        _navState.value = NavigationStates(_navState.value.triggerCounter, null, fallbackState)
    }
}

data class NavigationStates<S: NavBitNavigationState> (
    // Trigger counter is always incremented on a completed Interaction
    // That is to make sure that the UI is updated with the latest data, regardless if the NavigationState itself looks identical
    // For example if it contains the same id to an object, which itself has been modified
    val triggerCounter : Int,
    val old : S?,
    val current : S
)