package se.soderstrom.navbitdemo.navbit

import android.content.Context
import se.quidbit.navbit.NavBitNavigationStateHandler

class NavigationStateHandler : NavBitNavigationStateHandler<NavigationState, ScreenData>() {
    override fun fallbackStartScreenData(context: Context): ScreenData {
        return ScreenData.Start(0)
    }

    override fun prepareStartup(): NavigationState {
        return NavigationState.Start(0)
    }

    override fun onSettingState(state: NavigationState) {

    }
}