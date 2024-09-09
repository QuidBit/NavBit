package se.quidbit.navbit.updated.internal

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.quidbit.navbit.BuildConfig
import se.quidbit.navbit.internal.StringHelper
import se.quidbit.navbit.toimplement.NavBitActivity
import se.quidbit.navbit.toimplement.NavBitInteraction
import se.quidbit.navbit.toimplement.NavBitNavigationState
import se.quidbit.navbit.types.InteractionResult
import se.quidbit.navbit.updated.toimplement.NewBitInteractionHandler

internal class NewBitControllerFactory<I : NavBitInteraction, S : NavBitNavigationState>(private val interactionHandler: NewBitInteractionHandler<I, S>, private val startState: S) :
    ViewModelProvider.NewInstanceFactory()
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T = NewBitController(interactionHandler, startState) as T
}

internal class NewBitController<I : NavBitInteraction, S : NavBitNavigationState> (
    private val interactionHandler : NewBitInteractionHandler<I, S>,
    startState : S
) : ViewModel() {

    private val _navState = MutableStateFlow(startState)
    val navState: StateFlow<S> = _navState

    fun processInteraction(activity: ComponentActivity, interaction: I) {
        val currentState = _navState.value

        when (val interactionResult = interactionHandler.applyInteractionOnState(interaction, currentState, activity)) {
            is InteractionResult.Ignore -> {
                // Used when the view should not be updated by an interaction,
                // for example ignoring no longer relevant API calls coming in
            }
            is InteractionResult.Unexpected ->
                showError(activity, currentState,"Interaction", "Unexpected: [${StringHelper.prettyPrintSealedClassString(interaction.toString())}]")
            is InteractionResult.ErrorRead ->
                showError(activity, currentState,"Interaction", "Error Reading: [${interactionResult.error}]")
            is InteractionResult.CloseApp ->
                activity.finish()
            is InteractionResult.NewState ->
                _navState.value = interactionResult.state
        }
    }

    private fun showError(context: Context, currentState : S, source : String, error : String) {
        val infoString = "$error - ${currentState.prettyString()}"

        Log.e("NavBit", "$source $infoString")

        // If we are debugging, we can show the error directly on screen for simplicity
        NavBitActivity.mainHandler.post {
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