package se.soderstrom.navbitdemo.navbit

import se.quidbit.navbit.NavBitNavigationState

sealed class NavigationState : NavBitNavigationState() {
    class Start(var count : Int) : NavigationState()
    object Info : NavigationState()
    object InfoDetails : NavigationState()

    override fun deepCopy(): NavigationState {
        return when (this) {
            is Start -> Start(this.count)
            is Info -> Info
            is InfoDetails -> InfoDetails
        }
    }
}