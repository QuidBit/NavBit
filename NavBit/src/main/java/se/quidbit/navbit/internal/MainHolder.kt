package se.quidbit.navbit.internal

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import se.quidbit.navbit.toimplement.AppTheme
import se.quidbit.navbit.toimplement.NavBitInteraction
import se.quidbit.navbit.toimplement.NavBitNavigationState
import se.quidbit.navbit.toimplement.NavBitScreenHandler
import se.quidbit.navbit.types.OverlayScreen
import se.quidbit.navbit.types.QueuedInteraction
import se.quidbit.navbit.types.ScreenArrangement
import se.quidbit.navbit.types.ScreenOverlayType
import se.quidbit.navbit.types.ScreenTransition
import se.quidbit.navbit.types.TRANSITION_DURATION_DEFAULT_MS
import se.quidbit.navbit.types.TransitionFade
import se.quidbit.navbit.types.TransitionNone
import java.util.concurrent.BlockingQueue

const val OVERLAY_ANIMATION_MS = TRANSITION_DURATION_DEFAULT_MS

@Composable
internal fun <I : NavBitInteraction, S : NavBitNavigationState>
    MainHolder(
    context : Context,
    controller: MasterController<I, S>,
    screenHandler: NavBitScreenHandler<S>,
    interactionQueue : BlockingQueue<QueuedInteraction<I>>,
    theme : AppTheme
) {
    val navStates by controller.navState.collectAsState()

    // ----------------------------------------------
    // Check for the appropriate transition
    // ----------------------------------------------
    val transition = navStates.old?.let {
        screenHandler.transitionFromNavigationStates(it, navStates.current)
    } ?: TransitionFade()
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

            // NOTE: To get proper animations of the sheets in and out, a fixed supported count is used just now
            for (index in 0..< screenHandler.maxOverlayCount()) {
                OverlayLayer(index, screenArrangement, oldScreenArrangement, interactionQueue, transition)
            }
        }
    }
}

@Composable
fun <I : NavBitInteraction>OverlayLayer(
    index : Int,
    screenArrangement: ScreenArrangement,
    oldScreenArrangement: ScreenArrangement?,
    interactionQueue: BlockingQueue<QueuedInteraction<I>>,
    transition: ScreenTransition
) {
    val overlay = screenArrangement.overlays.getOrNull(index)

    // Darkening backdrop
    // -------------------------------------------------------------------------------
    AnimatedVisibility(
        visible = overlay != null,
        modifier = Modifier.fillMaxSize(),
        enter = fadeIn(animationSpec = tween(durationMillis = OVERLAY_ANIMATION_MS)),
        exit = fadeOut(animationSpec = tween(durationMillis = OVERLAY_ANIMATION_MS)),
        label = "OverlayTransition"
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        interactionQueue.add(QueuedInteraction.Close())
                    })
                }
        )
    }

    // Must retain the last overlay in order to animate it out even after it is gone removed
    // -------------------------------------------------------------------------------
    var displayOverlay by remember { mutableStateOf<OverlayScreen?>(null) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(overlay) {
        if (overlay != null) {
            displayOverlay = overlay
            visible = true
        } else {
            visible = false
            delay(OVERLAY_ANIMATION_MS.toLong())
            displayOverlay = null
        }
    }

    AnimatedVisibility(
        visible = visible && displayOverlay != null,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(OVERLAY_ANIMATION_MS)
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(OVERLAY_ANIMATION_MS)
        ),
        label = "OverlayTransition"
    ) {
        displayOverlay?.let { over ->
            val type = over.overlayType

            // Skip any transition of the content within the first time an overlay is shown
            // As in, this high index was not present among the old screens
            val overlayTransition =
                oldScreenArrangement?.overlays
                    ?.getOrNull(index)
                    ?.let { if (it.overlayType == type) transition else TransitionNone }
                    ?: TransitionNone

            when (type) {
                ScreenOverlayType.Sheet -> {
                    CustomSheet(
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(top = (index * 36).dp + 8.dp),
                        onClose = {
                            interactionQueue.add(QueuedInteraction.Close())
                        }
                    ) {
                        ScreenHolder(over.screen, overlayTransition)
                    }
                }

                ScreenOverlayType.Popup -> {
                    CustomPopup(modifier = Modifier) {
                        ScreenHolder(over.screen, overlayTransition)
                    }
                }
            }
        }
    }
}