package se.quidbit.navbit.internal

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        screenHandler.screenFromNavigationState(
            context,
            it
        ).asArrangement()
    }

    val newScreenArrangement = screenHandler.screenFromNavigationState(
        context,
        navStates.current
    ).asArrangement()

    val screenArrangement = newScreenArrangement ?: oldScreenArrangement

    if (newScreenArrangement == null) {
        controller.goToFallbackState()
    }

    // In the extreme case, where neither the previous state or the new state is valid, the screen will be left empty
    // However, this should never happen in practice, since when an invalid screen is found
    // we move to a fallback state that is expected to be valid
    screenArrangement?.let {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.holderBackgroundColor)
        ) {
            ScreenHolder(screenArrangement.main, transition)

            screenArrangement.overlays.forEachIndexed { index, overlay ->
                val type = overlay.overlayType

                // Skip any transition of the content within the first time an overlay is shown
                val overlayTransition = oldScreenArrangement?.overlays?.takeIf { it.size > index }?.get(index)?.let {
                    if (it.overlayType == type) transition else TransitionNone
                } ?: TransitionNone

                when (type) {
                    is ScreenOverlayType.Sheet -> {
                        ModalBottomSheet(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.statusBars)
                                .padding(top = (index * 36).dp + 16.dp),
                            onDismissRequest = {
                                if (!type.locked) {
                                    onBackPressedDispatcher?.onBackPressed()
                                }
                            },
                            sheetState = rememberModalBottomSheetState(true, confirmValueChange = {
                                !type.locked
                            }),
                            containerColor = theme.colorScheme.background,
                            dragHandle = if (type.handle) {
                                { BottomSheetDefaults.DragHandle() }
                            } else {
                                null
                            },
                            contentWindowInsets = {
                                if (type.inset) {
                                    BottomSheetDefaults. windowInsets
                                } else {
                                    WindowInsets(bottom = 0.dp) }
                                }
                        ) {
                            // ----------------------------------------------
                            // Take back interactions to prevent issue where the sheet is closed anyways
                            // ----------------------------------------------
                            BackHandler {
                                onBackPressedDispatcher?.onBackPressed()
                            }
                            // ----------------------------------------------

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
            }
        }
    }
}