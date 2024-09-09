package se.quidbit.navbit.updated.internal

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import se.quidbit.navbit.toimplement.NavBitInteraction
import se.quidbit.navbit.toimplement.NavBitNavigationState
import se.quidbit.navbit.updated.toimplement.NewBitScreenHandler
import se.quidbit.navbit.updated.types.InteractionReceiver

@Composable
internal fun <I : NavBitInteraction, S : NavBitNavigationState>
    MainHolder(context : Context, interactionReceiver: InteractionReceiver<I>, viewModel: NewBitController<I,S>, screenHandler: NewBitScreenHandler<I, S>)
{
    val navState by viewModel.navState.collectAsState()

    val screenArrangement = screenHandler.screenArrangementFromNavigationState(
        navState,
        interactionReceiver,
        context
    )

    ScreenHolder<I,S>(screenArrangement.main)
}