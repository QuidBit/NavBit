package se.soderstrom.navbitdemo.navbit

import se.quidbit.navbit.NavBitNavigationState

sealed class NavigationState : NavBitNavigationState() {
    object Start : NavigationState()
    object Info : NavigationState()
    object InfoDetails : NavigationState()

    override fun deepCopy(): NavigationState {
        return when (this) {
            Start -> Start
            Info -> Info
            InfoDetails -> InfoDetails
        }
    }
}