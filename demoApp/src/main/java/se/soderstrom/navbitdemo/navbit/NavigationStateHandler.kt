package se.soderstrom.navbitdemo.navbit

import android.content.Context
import se.quidbit.navbit.toimplement.NavBitNavigationStateHandler
import se.soderstrom.navbitdemo.BaseActivity

class NavigationStateHandler : NavBitNavigationStateHandler<NavigationState>() {

    override fun loadStartupNavigationState(): NavigationState {
        // Load any previously stored count
        val sharedPreferences = BaseActivity.instance().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val count = sharedPreferences.getInt("count", 0)

        BaseActivity.instance().send(Interaction.GoToNext)
        return NavigationState.Start(count)
    }

    override fun onNavigatingToNewState(state: NavigationState) {
        // ---------------------------------------------------------
        // Store the state for app restarts
        // ---------------------------------------------------------
        val count = when (state) {
            is NavigationState.Start -> state.count
            is NavigationState.ClearCheck -> state.count
            is NavigationState.ClearCheckAgain -> state.count
            is NavigationState.Info -> state.count
            is NavigationState.InfoDetails -> state.count
            is NavigationState.ScreenA -> state.count
            is NavigationState.ScreenB -> state.count
        }

        val sharedPreferences = BaseActivity.instance().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("count", count)
        editor.apply()
    }
}