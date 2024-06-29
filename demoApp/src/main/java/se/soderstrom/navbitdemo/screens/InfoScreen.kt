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

class InfoScreen(context : Context) : Screen<ScreenData.Info>(context) {

    override fun getLayoutIds(type: ScreenType): ScreenLayoutIds {
        return ScreenLayoutIds(null, R.layout.screen_info)
    }

    override fun prepareLayout(view: View, type: ScreenType): ScreenInsets {
        val detailsButton = view.findViewById<MaterialCardView>(R.id.details_button)
        detailsButton.setOnClickListener{
            EventBus.getDefault().post(Interaction.ViewInfoDetails())
        }

        val doneButton = view.findViewById<MaterialCardView>(R.id.done_button)
        doneButton.setOnClickListener{
            EventBus.getDefault().post(Interaction.Done())
        }

        return ScreenInsets()
    }

    override fun entering(newData: ScreenData.Info, notifyReady: () -> Unit) {}

    override fun updating(oldData: ScreenData.Info, newData: ScreenData.Info) {}

    override fun returning(oldData: ScreenData.Info, newData: ScreenData.Info) {}

    override fun getBackgroundWork(): BackgroundWork? {
        return null
    }
}