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
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.channels.Channel
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
import kotlin.math.max

const val OVERLAY_ANIMATION_MS = TRANSITION_DURATION_DEFAULT_MS

@Composable
internal fun <I : NavBitInteraction, S : NavBitNavigationState>
    MainHolder(
    context : Context,
    controller: MasterController<I, S>,
    screenHandler: NavBitScreenHandler<S>,
    interactionChannel : Channel<QueuedInteraction<I>>,
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
                .background(theme.holderBackgroundColor),
            contentAlignment = Alignment.BottomCenter // Sheet are at the bottom, and popups fill the screen so it is not affected
        ) {
            ScreenHolder(screenArrangement.main, transition)

            // NOTE: To get proper animations of the sheets in and out, a fixed supported count is used just now
            for (index in 0..< screenHandler.maxOverlayCount()) {
                OverlayLayer(index, screenArrangement, oldScreenArrangement, interactionChannel, transition)
            }
        }
    }
}

@Composable
fun <I : NavBitInteraction>OverlayLayer(
    index : Int,
    screenArrangement: ScreenArrangement,
    oldScreenArrangement: ScreenArrangement?,
    interactionChannel: Channel<QueuedInteraction<I>>,
    transition: ScreenTransition
) {
    val overlay = screenArrangement.overlays.getOrNull(index)

    val closingTime = remember { mutableStateOf<Int?>(null) }

    val isVisible = overlay != null

    // -------------------------------------------------------------------------------
    // Clear any focus/input whenever an overlay appears/is removed
    // -------------------------------------------------------------------------------
    val focusManager = LocalFocusManager.current
    var wasVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        if (isVisible != wasVisible) {
            wasVisible = isVisible
            focusManager.clearFocus(force = true)
        }
    }

    // -------------------------------------------------------------------------------
    // Darkening backdrop
    // -------------------------------------------------------------------------------

    val openingFadeTime = OVERLAY_ANIMATION_MS + 250 // Have the darkening finishing slightly after on open seems to look better
    val closingFadeTime = max(closingTime.value ?: 0, OVERLAY_ANIMATION_MS) // Use the standard overlay animation time as a minimum to reduce "jumping" feeling on fast close

    val updatedOverlay by rememberUpdatedState(overlay)
    val updatedClosingTime by rememberUpdatedState(closingTime)

    AnimatedVisibility(
        visible = isVisible,
        modifier = Modifier.fillMaxSize(),
        enter = fadeIn(animationSpec = tween(durationMillis = openingFadeTime)),
        exit = fadeOut(animationSpec = tween(durationMillis = closingFadeTime)),
        label = "OverlayTransition"
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        if (updatedOverlay != null && updatedClosingTime.value == null) {
                            interactionChannel.trySend(QueuedInteraction.Close())
                        }
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
            delay(closingFadeTime.toLong())
            displayOverlay = null
            closingTime.value = null
        }
    }

    // Retain the visibility when a drag is closing the sheet, to not get two animations pulling out the sheet over each other (incorrect, too fast speed)
    // NOTE: Does not use the closing animation time since is not used during dragging
    AnimatedVisibility(
        visible = (visible && displayOverlay != null) || closingTime.value != null,
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
                is ScreenOverlayType.Sheet -> {
                    CustomSheet(
                        type.locked,
                        type.maxWidth,
                        closingTime,
                        modifier = Modifier
                            .windowInsetsPadding(
                                WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Start + WindowInsetsSides.End)
                            )
                            .padding(top = (index * 36).dp + 8.dp),
                        onClose = {
                            interactionChannel.trySend(QueuedInteraction.Close())
                        }
                    ) {
                        ScreenHolder(over.screen, overlayTransition)
                    }
                }

                is ScreenOverlayType.Popup -> {
                    CustomPopup(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
                        ScreenHolder(over.screen, overlayTransition)
                    }
                }
            }
        }
    }
}