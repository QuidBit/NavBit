package se.soderstrom.navbitdemo.navbit

import android.content.Context
import se.quidbit.navbit.NavBitNavigationStateHandler

class NavigationStateHandler : NavBitNavigationStateHandler<NavigationState, ScreenData>() {
    override fun fallbackStartScreenData(context: Context): ScreenData {
        return ScreenData.Start
    }

    override fun initializeAndGetStartState(): NavigationState {
        return NavigationState.Start
    }

    override fun onSettingState(state: NavigationState) {

    }
}