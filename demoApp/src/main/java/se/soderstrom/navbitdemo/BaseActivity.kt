package se.soderstrom.navbitdemo

import se.quidbit.navbit.updated.toimplement.NewBitActivity
import se.soderstrom.navbitdemo.newnavbit.InteractionHandler
import se.soderstrom.navbitdemo.newnavbit.Interaction
import se.soderstrom.navbitdemo.newnavbit.NavigationState
import se.soderstrom.navbitdemo.newnavbit.NewNavigationStateHandler
import se.soderstrom.navbitdemo.newnavbit.NewScreenHandler

class BaseActivity : NewBitActivity<Interaction, NavigationState>(
    InteractionHandler(),
    NewNavigationStateHandler(),
    NewScreenHandler(),
) {
    companion object {
        fun getNavBit() : NewBitActivity<Interaction, NavigationState> {
            return getNavBitInstance()
        }
    }
}