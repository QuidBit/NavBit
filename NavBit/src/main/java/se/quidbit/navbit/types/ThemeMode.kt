package se.quidbit.navbit.types

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    @Composable
    fun isCurrentlyDark() : Boolean {
        return when (this) {
            DARK -> true
            LIGHT -> false
            SYSTEM -> isSystemInDarkTheme()
        }
    }
}