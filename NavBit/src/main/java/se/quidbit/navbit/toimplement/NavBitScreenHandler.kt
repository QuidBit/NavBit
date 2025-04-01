package se.quidbit.navbit.toimplement

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import se.quidbit.navbit.types.ScreenResult
import se.quidbit.navbit.types.ScreenTransition

abstract class NavBitScreenHandler<S : NavBitNavigationState> {
    abstract fun screenFromNavigationState(context : Context, s : S,) : ScreenResult
    abstract fun transitionFromNavigationStates(old : S, new : S) : ScreenTransition
    abstract fun getTheme(context : Context, isDarkMode : Boolean) : AppTheme
    abstract fun maxOverlayCount() : Int
}

data class AppTheme (
    val colorScheme: ColorScheme,
    val typography: Typography,
    val holderBackgroundColor : Color,
)