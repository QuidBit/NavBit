package se.quidbit.navbit.internal

import android.content.Context
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import se.quidbit.navbit.toimplement.AppTheme
import se.quidbit.navbit.toimplement.NavBitInteraction
import se.quidbit.navbit.toimplement.NavBitNavigationState
import se.quidbit.navbit.toimplement.NavBitScreenHandler
import se.quidbit.navbit.types.ScreenOverlayType
import se.quidbit.navbit.types.TransitionFade
import se.quidbit.navbit.types.TransitionNone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <I : NavBitInteraction, S : NavBitNavigationState>
    MainHolder(context : Context, controller: MasterController<I, S>, screenHandler: NavBitScreenHandler<S>, theme : AppTheme)
{
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val navStates by controller.navState.collectAsState()

    // ----------------------------------------------
    // Check for the appropriate transition
    // ----------------------------------------------
    val transition = navStates.old?.let {
        screenHandler.transitionFromNavigationStates(it, navStates.current)
    } ?: TransitionFade
    // ----------------------------------------------

    val oldScreenArrangement = navStates.old?.let {
        screenHandler.screenArrangementFromNavigationState(
            context,
            it
        )
    }

    val screenArrangement = screenHandler.screenArrangementFromNavigationState(
        context,
        navStates.current
    )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.holderBackgroundColor)
        ) {
            ScreenHolder(screenArrangement.main, transition)

            screenArrangement.overlays.forEachIndexed { index, overlay ->
                // Skip any transition of the content within the first time an overlay is shown
                val overlayTransition = oldScreenArrangement?.overlays?.takeIf { it.size > index }?.get(index)?.let {
                    if (it.overlayType == overlay.overlayType) transition else TransitionNone
                } ?: TransitionNone

                when (overlay.overlayType) {
                    ScreenOverlayType.Sheet -> {
                        ModalBottomSheet(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.systemBars)
                                .padding(top = (index * 36).dp + 16.dp),
                            onDismissRequest = {
                                onBackPressedDispatcher?.onBackPressed()
                            },
                            sheetState = rememberModalBottomSheetState(true),
                            containerColor = theme.colorScheme.background
                        ) {
                            ScreenHolder(overlay.screen, overlayTransition)
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
                                colors = CardDefaults.cardColors(
                                    containerColor = theme.colorScheme.background
                                ),
                            ) {
                                ScreenHolder(overlay.screen, overlayTransition)
                            }
                        }
                    }
                }
            }}

}