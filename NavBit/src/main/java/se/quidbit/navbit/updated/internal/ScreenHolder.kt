package se.quidbit.navbit.updated.internal

import androidx.compose.animation.*
import androidx.compose.runtime.*
import se.quidbit.navbit.toimplement.NavBitInteraction
import se.quidbit.navbit.toimplement.NavBitNavigationState
import se.quidbit.navbit.updated.types.ScreenComposable
import se.quidbit.navbit.updated.types.TransitionHelper

@Composable
internal fun <I : NavBitInteraction, S : NavBitNavigationState>
    ScreenHolder(screenComposable: ScreenComposable)
{
    val previousScreenId = remember { mutableStateOf(screenComposable.id) }

    // Determine if the screen id has changed
    val hasScreenChanged = previousScreenId.value != screenComposable.id

    // Update the current screen id
    previousScreenId.value = screenComposable.id

     AnimatedContent(
        targetState = screenComposable,
        transitionSpec = {
            if (hasScreenChanged) {
                TransitionHelper.slideInTransition()
            } else {
                EnterTransition.None togetherWith ExitTransition.None
            }
        },
         label = ""
    ) { screen  ->
         screen.screen()
    }
}