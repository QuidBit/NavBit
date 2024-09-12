package se.soderstrom.navbitdemo

import se.quidbit.navbit.toimplement.NavBitActivity
import se.soderstrom.navbitdemo.navbit.InteractionHandler
import se.soderstrom.navbitdemo.navbit.Interaction
import se.soderstrom.navbitdemo.navbit.NavigationState
import se.soderstrom.navbitdemo.navbit.NavigationStateHandler
import se.soderstrom.navbitdemo.navbit.ScreenHandler

class BaseActivity : NavBitActivity<Interaction, NavigationState>(
    InteractionHandler(),
    NavigationStateHandler(),
    ScreenHandler(),
) {
    companion object {
        fun instance() : NavBitActivity<Interaction, NavigationState> {
            return getNavBitInstance()
        }
    }
}