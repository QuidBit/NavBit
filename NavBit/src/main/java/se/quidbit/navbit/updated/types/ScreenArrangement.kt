package se.quidbit.navbit.updated.types

import androidx.compose.runtime.Composable

data class ScreenArrangement (
    val main : ScreenComposable,
    val overlays : List<OverlayScreen> = ArrayList()
) {
    companion object {
        fun main(id : String, compose : @Composable () -> Unit) : ScreenArrangement {
            return ScreenArrangement(ScreenComposable(id, compose))
        }
    }
}

data class OverlayScreen (
    val overlayTYpe : ScreenOverlayType,
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