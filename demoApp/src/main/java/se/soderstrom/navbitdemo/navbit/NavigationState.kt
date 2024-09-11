package se.soderstrom.navbitdemo.navbit

import se.quidbit.navbit.toimplement.NavBitNavigationState

sealed class NavigationState : NavBitNavigationState() {
    data class Start(val count : Int) : NavigationState()
    data class ClearCheck(val count : Int) : NavigationState()
    data class ClearCheckAgain(val count : Int) : NavigationState()
    data class Info(val count : Int) : NavigationState()
    data class InfoDetails(val count : Int, val expanded : Boolean) : NavigationState()
    data class ScreenA(val count : Int) : NavigationState()
    data class ScreenB(val count : Int) : NavigationState()
}