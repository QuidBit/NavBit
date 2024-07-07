package se.soderstrom.navbitdemo

import android.os.Bundle
import se.quidbit.navbit.NavBitActivity
import se.soderstrom.navbitdemo.navbit.InteractionHandler
import se.soderstrom.navbitdemo.navbit.Interaction
import se.soderstrom.navbitdemo.navbit.NavigationState
import se.soderstrom.navbitdemo.navbit.NavigationStateHandler
import se.soderstrom.navbitdemo.navbit.ScreenData
import se.soderstrom.navbitdemo.navbit.ScreenHandler

class BaseActivity : NavBitActivity<Interaction, NavigationState, ScreenData>(
    interactionHandler,
    stateHandler,
    screenHandler
) {
    companion object {
        // Retain all handlers between rotations
            // Especially important for the stateHandler so the app state is not lost
        val interactionHandler = InteractionHandler()
        val stateHandler = NavigationStateHandler()
        val screenHandler = ScreenHandler()

        // Allow easy access to NavBit from anywhere in the app
        fun getNavBit() : NavBitActivity<Interaction, NavigationState, ScreenData> {
            return getNavBitInstance()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeNavBit()
    }
}