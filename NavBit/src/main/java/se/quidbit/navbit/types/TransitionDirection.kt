package se.quidbit.navbit.types

enum class TransitionDirection {
    Forward,
    Backward;

    fun opposite() : TransitionDirection {
        return when (this) {
            Forward -> Backward
            Backward -> Forward
        }
    }
}