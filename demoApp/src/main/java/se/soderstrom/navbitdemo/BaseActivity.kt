package se.soderstrom.navbitdemo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import org.greenrobot.eventbus.EventBus
import se.quidbit.navbit.NavBitActivity
import se.soderstrom.navbitdemo.navbit.InteractionHandler
import se.soderstrom.navbitdemo.navbit.Interaction
import se.soderstrom.navbitdemo.navbit.NavigationState
import se.soderstrom.navbitdemo.navbit.NavigationStateHandler
import se.soderstrom.navbitdemo.navbit.ScreenData
import se.soderstrom.navbitdemo.navbit.ScreenHandler

class BaseActivity : NavBitActivity<Interaction, NavigationState, ScreenData>(
    InteractionHandler(),
    NavigationStateHandler(),
    ScreenHandler()
) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeNavigation()

        Handler(Looper.getMainLooper()).postDelayed({
           EventBus.getDefault().post(Interaction.ViewInfo())
        }, 6000)

        Handler(Looper.getMainLooper()).postDelayed({
            EventBus.getDefault().post(Interaction.ViewInfoDetails())
        }, 12000)
    }

    companion object {
        fun getNavBit() : NavBitActivity<Interaction, NavigationState, ScreenData> {
            return getNavBitInstance()
        }
    }
}