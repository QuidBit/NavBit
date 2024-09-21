package se.quidbit.navbit.toimplement

abstract class NavBitNavigationStateHandler <S : NavBitNavigationState> {
    abstract fun loadStartupNavigationState() : S
    abstract fun onNavigatingToNewState(state : S)
    abstract fun getFallbackState(state : S) : S
}