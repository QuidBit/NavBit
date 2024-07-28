package se.quidbit.navbit.types

// ----------------------------------------------------

sealed class TransitionType {
    object Sheet : TransitionType()
    object PopUp : TransitionType()
    sealed class Full : TransitionType() {
        object Fade : Full()
        object Slide : Full()
    }

    fun previousIsVisible() : Boolean {
        return when (this) {
            is Sheet,
            is PopUp -> true
            is Full -> false
        }
    }
}

// ----------------------------------------------------

enum class TransitionDirection {
    Forward,
    Backward
}