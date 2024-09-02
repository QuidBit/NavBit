package se.soderstrom.navbitdemo.navbit

import android.content.Context
import se.quidbit.navbit.toimplement.NavBitNavigationStateHandler
import se.quidbit.navbit.types.ScreenType
import se.soderstrom.navbitdemo.BaseActivity

class NavigationStateHandler : NavBitNavigationStateHandler<NavigationState, ScreenData>() {
    override fun fallbackStartupScreenData(context: Context): ScreenData {
        return ScreenData.Start(0)
    }

    override fun loadStartupNavigationState(): NavigationState {
        // Load any previously stored count
        val sharedPreferences = BaseActivity.getNavBit().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val count = sharedPreferences.getInt("count", 0)

        return NavigationState.Start(count)
    }

    override fun storeStartupNavigationState(state: NavigationState) {
        // Save the counter for app restarts
        val count = when (state) {
            is NavigationState.ClearCheck -> state.count
            is NavigationState.Info -> state.count
            is NavigationState.InfoDetails -> state.count
            is NavigationState.Start -> state.count
            is NavigationState.Timer -> state.count
            is NavigationState.Fast -> state.count
            is NavigationState.Slow -> state.count
        }

        val sharedPreferences = BaseActivity.getNavBit().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("count", count)
        editor.apply()
    }

    override fun getScreenToPreload(state: NavigationState): Pair<ScreenData, ScreenType>? {
        return when (state) {
            is NavigationState.Start -> Pair(ScreenData.Timer, ScreenType.Full)
            is NavigationState.Info -> Pair(ScreenData.InfoDetails(false), ScreenType.Sheet)
            else -> null
        }
    }

    override fun onNavigatingToNewState(state: NavigationState) {

    }
}