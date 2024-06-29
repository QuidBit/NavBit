package se.soderstrom.navbitdemo.screens

import android.content.Context
import android.view.View
import com.google.android.material.card.MaterialCardView
import org.greenrobot.eventbus.EventBus
import se.quidbit.navbit.BackgroundWork
import se.quidbit.navbit.Screen
import se.quidbit.navbit.ScreenInsets
import se.quidbit.navbit.ScreenLayoutIds
import se.quidbit.navbit.ScreenType
import se.soderstrom.navbitdemo.R
import se.soderstrom.navbitdemo.navbit.Interaction
import se.soderstrom.navbitdemo.navbit.ScreenData

class StartScreen(context : Context) : Screen<ScreenData.Start>(context) {
    override fun getLayoutIds(type: ScreenType): ScreenLayoutIds {
        return ScreenLayoutIds(R.layout.screen_start, null)
    }

    override fun prepareLayout(view: View, type: ScreenType): ScreenInsets {
        val tabLogin = view.findViewById<MaterialCardView>(R.id.info_button)
        tabLogin.setOnClickListener{
            EventBus.getDefault().post(Interaction.ViewInfo())
        }

        return ScreenInsets()
    }

    override fun entering(newData: ScreenData.Start, notifyReady: () -> Unit) {}

    override fun updating(oldData: ScreenData.Start, newData: ScreenData.Start) {}

    override fun returning(oldData: ScreenData.Start, newData: ScreenData.Start) {}

    override fun getBackgroundWork(): BackgroundWork? {
        return null
    }
}