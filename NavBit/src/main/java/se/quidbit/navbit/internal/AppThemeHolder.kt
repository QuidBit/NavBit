package se.quidbit.navbit.internal

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import se.quidbit.navbit.toimplement.AppTheme

@Composable
internal fun AppThemeHolder(theme: AppTheme, content : @Composable () -> Unit) {
    MaterialTheme (
        colorScheme = theme.colorScheme,
        typography = theme.typography,
        content =  content
    )
}