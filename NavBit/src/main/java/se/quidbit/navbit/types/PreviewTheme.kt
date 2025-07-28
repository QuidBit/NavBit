package se.quidbit.navbit.types

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import se.quidbit.navbit.internal.AppThemeHolder
import se.quidbit.navbit.toimplement.NavBitNavigationState
import se.quidbit.navbit.toimplement.NavBitScreenHandler

@Composable
fun <S : NavBitNavigationState>PreviewTheme(screenHandler: NavBitScreenHandler<S>, showBackground : Boolean, content : @Composable () -> Unit) {
    val theme = screenHandler.getTheme(LocalContext.current, isSystemInDarkTheme())
    if (showBackground) {
        Box(
            Modifier.background(theme.colorScheme.background)
        ) {
            AppThemeHolder(theme, content)
        }
    } else {
        AppThemeHolder(theme, content)
    }
}