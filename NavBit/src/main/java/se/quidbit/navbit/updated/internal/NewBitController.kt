package se.quidbit.navbit.updated.internal

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
import se.quidbit.navbit.internal.StringHelper
import se.quidbit.navbit.toimplement.NavBitInteraction
import se.quidbit.navbit.toimplement.NavBitNavigationState
import se.quidbit.navbit.types.InteractionResult
import se.quidbit.navbit.updated.toimplement.NewBitInteractionHandler
import se.quidbit.navbit.updated.toimplement.NewBitNavigationStateHandler

internal class NewBitControllerFactory<I : NavBitInteraction, S : NavBitNavigationState> (
    private val interactionHandler: NewBitInteractionHandler<I, S>,
    private val navigationStateHandler: NewBitNavigationStateHandler<S>
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = NewBitController(interactionHandler, navigationStateHandler) as T
}

internal class NewBitController<I : NavBitInteraction, S : NavBitNavigationState> (
    private val interactionHandler : NewBitInteractionHandler<I, S>,
    private val navigationStateHandler: NewBitNavigationStateHandler<S>
) : ViewModel() {

    private val _navState = MutableStateFlow(NavigationStates(null, navigationStateHandler.loadStartupNavigationState()))
    val  navState: StateFlow<NavigationStates<S>> = _navState

    fun processInteraction(activity: ComponentActivity, interaction : QueuedInteraction<I>) {
        val currentState = _navState.value.current

        val interactionResult = when(interaction) {
            is QueuedInteraction.Back -> interactionHandler.applyBackInteractionOnState(currentState)
            is QueuedInteraction.Custom -> interactionHandler.applyInteractionOnState(interaction.interaction, currentState, activity)
        }

        when (interactionResult) {
            is InteractionResult.Ignore -> {
                // Used when the view should not be updated by an interaction,
                // for example ignoring no longer relevant API calls coming in
            }
            is InteractionResult.Unexpected ->
                showError(activity, currentState,"Interaction", "Unexpected: [${ StringHelper.prettyPrintSealedClassString(interaction.toString())}")
            is InteractionResult.ErrorRead ->
                showError(activity, currentState,"Interaction", "Error Reading: [${interactionResult.error}]")
            is InteractionResult.CloseApp ->
                activity.finish()
            is InteractionResult.NewState -> {
                val newState = interactionResult.state
                navigationStateHandler.onNavigatingToNewState(newState)
                Log.i("NavBit", "New State: $newState")
                _navState.value = NavigationStates(_navState.value.current, newState)
            }
        }
    }

    private fun showError(context: Context, currentState : S, source : String, error : String) {
        val infoString = "$error - ${currentState.prettyString()}"

        Log.e("NavBit", "$source $infoString")

        // If we are debugging, we can show the error directly on screen for simplicity
        Handler(Looper.getMainLooper()).post {
            if (BuildConfig.DEBUG) {
                Toast.makeText(
                    context,
                    "$source - $infoString",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

data class NavigationStates<S: NavBitNavigationState> (
    val old : S?,
    val current : S
)