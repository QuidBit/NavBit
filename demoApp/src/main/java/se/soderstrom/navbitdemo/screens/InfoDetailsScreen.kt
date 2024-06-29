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

class InfoDetailsScreen(context : Context) : Screen<ScreenData.InfoDetails>(context) {

    override fun getLayoutIds(type: ScreenType): ScreenLayoutIds {
        return ScreenLayoutIds(null, R.layout.screen_info_details)
    }

    override fun prepareLayout(view: View, type: ScreenType): ScreenInsets {
        val doneButton = view.findViewById<MaterialCardView>(R.id.done_button)
        doneButton.setOnClickListener{
            EventBus.getDefault().post(Interaction.Done())
        }

        return ScreenInsets(null, null, view, R.dimen.sheet_bottom)
    }

    override fun entering(newData: ScreenData.InfoDetails, notifyReady: () -> Unit) {
        notifyReady()
    }

    override fun updating(oldData: ScreenData.InfoDetails, newData: ScreenData.InfoDetails) {}

    override fun returning(oldData: ScreenData.InfoDetails, newData: ScreenData.InfoDetails) {}

    override fun getBackgroundWork(): BackgroundWork? {
        return null
    }
}