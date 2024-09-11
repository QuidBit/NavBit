package se.quidbit.navbit.updated.toimplement

import se.quidbit.navbit.toimplement.NavBitNavigationState

abstract class NewBitNavigationStateHandler <S : NavBitNavigationState> {
    abstract fun loadStartupNavigationState() : S
    abstract fun onNavigatingToNewState(state : S)
}