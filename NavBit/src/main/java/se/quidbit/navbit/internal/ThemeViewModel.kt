package se.quidbit.navbit.internal

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import se.quidbit.navbit.types.ThemeMode

class ThemeViewModel : ViewModel() {
    private val _themeMode = mutableStateOf(ThemeMode.SYSTEM)
    val themeMode: State<ThemeMode> get() = _themeMode

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }
}
