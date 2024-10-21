package se.quidbit.navbit.types

import androidx.compose.runtime.Composable

data class ScreenArrangement(
    val main: ScreenComposable,
    val overlays: ArrayList<OverlayScreen> = ArrayList()
) {
    constructor(
        id: String,
        compose: @Composable () -> Unit
    ) : this(
        ScreenComposable(id, compose)
    )

    fun addOverlay(overlayType: ScreenOverlayType, id : String, compose: @Composable () -> Unit) {
        overlays.add(OverlayScreen(overlayType, ScreenComposable(id, compose)))
    }
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