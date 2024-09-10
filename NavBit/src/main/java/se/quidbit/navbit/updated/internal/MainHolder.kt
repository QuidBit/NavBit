package se.quidbit.navbit.updated.internal

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import se.quidbit.navbit.toimplement.NavBitInteraction
import se.quidbit.navbit.toimplement.NavBitNavigationState
import se.quidbit.navbit.types.TransitionDirection
import se.quidbit.navbit.updated.toimplement.NewBitScreenHandler
import se.quidbit.navbit.updated.types.InteractionReceiver
import se.quidbit.navbit.updated.types.ScreenTransitionSet
import se.quidbit.navbit.updated.types.StandardTransitions

@Composable
internal fun <I : NavBitInteraction, S : NavBitNavigationState>
    MainHolder(context : Context, interactionReceiver: InteractionReceiver<I>, viewModel: NewBitController<I,S>, screenHandler: NewBitScreenHandler<I, S>)
{
    val navStates by viewModel.navState.collectAsState()

    Log.e("NavBit", "okay, updating main holder...")

    // ----------------------------------------------
    // Check for the appropriate transition
    // ----------------------------------------------
    val transitionSet = navStates.old?.let {
        screenHandler.transitionFromNavigationStates(it, navStates.current)
    } ?: ScreenTransitionSet()
    // ----------------------------------------------

    val screenArrangement = screenHandler.screenArrangementFromNavigationState(
        navStates.current,
        interactionReceiver,
        context
    )

    Log.e("NavBit", "before screen holder...")
    ScreenHolder<I,S>(screenArrangement.main, transitionSet)
}