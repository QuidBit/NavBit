package se.soderstrom.navbitdemo.navbit

import se.quidbit.navbit.toimplement.NavBitNavigationState

sealed class NavigationState : NavBitNavigationState() {
    class Start(var count : Int) : NavigationState()
    class ClearCheck(var count : Int) : NavigationState()
    class Info(var count : Int) : NavigationState()
    class InfoDetails(var count : Int, var expanded : Boolean) : NavigationState()
    class Timer(var count : Int) : NavigationState()
    class Slow(var count : Int) : NavigationState()
    class Fast(var count : Int) : NavigationState()


    override fun deepCopy(): NavigationState {
        return when (this) {
            is Start -> Start(count)
            is ClearCheck -> ClearCheck(count)
            is Info -> Info(count)
            is InfoDetails -> InfoDetails(count, expanded)
            is Timer -> Timer(count)
            is Fast -> Fast(count)
            is Slow -> Slow(count)
        }
    }
}