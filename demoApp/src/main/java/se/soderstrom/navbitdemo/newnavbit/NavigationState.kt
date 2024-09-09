package se.soderstrom.navbitdemo.newnavbit

import se.quidbit.navbit.toimplement.NavBitNavigationState

sealed class NavigationState : NavBitNavigationState() {
    data class Start(val count : Int) : NavigationState()
    data class ClearCheck(val count : Int) : NavigationState()
    data class Info(val count : Int) : NavigationState()
    data class InfoDetails(val count : Int, val expanded : Boolean) : NavigationState()
    data class Timer(val count : Int) : NavigationState()
    data class Slow(val count : Int) : NavigationState()
    data class Fast(val count : Int) : NavigationState()
}