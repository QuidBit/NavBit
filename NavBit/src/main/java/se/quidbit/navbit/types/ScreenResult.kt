package se.quidbit.navbit.types

sealed class ScreenResult {
    data object InvalidState : ScreenResult()
    data class Arrangement(val arrangement : ScreenArrangement) : ScreenResult()

    fun asArrangement() : ScreenArrangement? {
        return when (this) {
            is Arrangement -> this.arrangement
            is InvalidState -> null
        }
    }
}