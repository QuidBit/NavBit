package se.soderstrom.navbitdemo.navbit

import se.quidbit.navbit.toimplement.NavBitNavigationState

sealed class NavigationState : NavBitNavigationState() {
    data class Start(override val count : Int) : NavigationState(), StartBasedState
    data class Info(override val count : Int) : NavigationState(), StartBasedState
    data class InfoDetails(override val count : Int, val expanded : Boolean) : NavigationState(), StartBasedState
    data class ClearCheck(override val count : Int) : NavigationState(), StartBasedState
    data class ClearCheckAgain(override val count : Int) : NavigationState(), StartBasedState

    data class ScreenA(val count : Int) : NavigationState()
    data class ScreenB(val count : Int) : NavigationState()
}

sealed interface StartBasedState {
    val count : Int
}