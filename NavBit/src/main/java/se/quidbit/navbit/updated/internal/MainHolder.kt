package se.quidbit.navbit.updated.internal

import android.content.Context
import android.util.Log
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import se.quidbit.navbit.toimplement.NavBitInteraction
import se.quidbit.navbit.toimplement.NavBitNavigationState
import se.quidbit.navbit.updated.toimplement.NewBitScreenHandler
import se.quidbit.navbit.updated.types.InteractionReceiver
import se.quidbit.navbit.updated.types.ScreenOverlayType
import se.quidbit.navbit.updated.types.ScreenTransitionSet
import se.quidbit.navbit.updated.types.TransitionFade

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <I : NavBitInteraction, S : NavBitNavigationState>
    MainHolder(context : Context, interactionReceiver: InteractionReceiver<I>, viewModel: NewBitController<I,S>, screenHandler: NewBitScreenHandler<I, S>)
{
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val navStates by viewModel.navState.collectAsState()

    Log.e("NavBit", "okay, updating main holder...")

    // ----------------------------------------------
    // Check for the appropriate transition
    // ----------------------------------------------
    val transitionSet = navStates.old?.let {
        screenHandler.transitionFromNavigationStates(it, navStates.current)
    } ?: TransitionFade
    // ----------------------------------------------

    val screenArrangement = screenHandler.screenArrangementFromNavigationState(
        navStates.current,
        interactionReceiver,
        context
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenHandler.mainBackgroundColor())
    ) {
        Log.e("NavBit", "before screen holder...")
        ScreenHolder<I,S>(screenArrangement.main, transitionSet)

        screenArrangement.overlays.forEachIndexed { index, overlay ->
            when (overlay.overlayType) {
                ScreenOverlayType.Sheet -> {
                    ModalBottomSheet(
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.systemBars)
                            .padding(top = (index * 36).dp + 16.dp),
                        onDismissRequest = {
                            onBackPressedDispatcher?.onBackPressed()
                        },
                        sheetState = rememberModalBottomSheetState(true)
                    ) {
                        ScreenHolder<I,S>(overlay.screen, transitionSet)
                    }
                }

                ScreenOverlayType.Popup -> {
                    Dialog(
                        onDismissRequest = {
                            onBackPressedDispatcher?.onBackPressed()
                        }
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(Alignment.CenterVertically),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            ScreenHolder<I,S>(overlay.screen, transitionSet)
                        }
                    }
                }
            }
        }
    }
}