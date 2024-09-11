package se.quidbit.navbit.types

import androidx.compose.runtime.Composable

data class ScreenArrangement(
    val main: ScreenComposable,
    val overlays: List<OverlayScreen> = ArrayList()
) {
    // Constructor for the main screen only
    constructor(id: String, compose: @Composable () -> Unit) : this(
        ScreenComposable(id, compose)
    )

    // Constructor for one overlay
    constructor(
        id: String, compose: @Composable () -> Unit,
        firstType: ScreenOverlayType, firstId: String, firstCompose: @Composable () -> Unit
    ) : this(
        ScreenComposable(id, compose),
        listOf(
            OverlayScreen(firstType, ScreenComposable(firstId, firstCompose))
        )
    )

    // Constructor for two overlays
    constructor(
        id: String, compose: @Composable () -> Unit,
        firstType: ScreenOverlayType, firstId: String, firstCompose: @Composable () -> Unit,
        secondType: ScreenOverlayType, secondId: String, secondCompose: @Composable () -> Unit
    ) : this(
        ScreenComposable(id, compose),
        listOf(
            OverlayScreen(firstType, ScreenComposable(firstId, firstCompose)),
            OverlayScreen(secondType, ScreenComposable(secondId, secondCompose))
        )
    )
}

data class OverlayScreen (
    val overlayType : ScreenOverlayType,
    val screen : ScreenComposable
)

enum class ScreenOverlayType {
    Sheet,
    Popup
}

data class ScreenComposable (
    val id : String,
    val content : @Composable () -> Unit
)