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

class PopupClearScreen(context : Context) : Screen<ScreenData.ClearCheck>(context) {

    override fun getLayoutIds(type: ScreenType): ScreenLayoutIds {
        return ScreenLayoutIds(R.layout.screen_clear)
    }

    override fun prepareLayout(view: View, type: ScreenType, onPrepared: (ScreenInsets) -> Unit) {
        val yesButton = view.findViewById<MaterialCardView>(R.id.yesButton)
        yesButton.setOnClickListener{
            EventBus.getDefault().post(Interaction.ClearPerform)
        }

        val noButton = view.findViewById<MaterialCardView>(R.id.noButton)
        noButton.setOnClickListener{
            EventBus.getDefault().post(Interaction.Back)
        }

        onPrepared(ScreenInsets())
    }

    override fun entering(data: ScreenData.ClearCheck, releaseForDisplay: (workAfter : () -> Unit) -> Unit) {
        releaseForDisplay{}
    }

    override fun updating(oldData: ScreenData.ClearCheck, data: ScreenData.ClearCheck) {}

    override fun returning(oldData: ScreenData.ClearCheck?, data: ScreenData.ClearCheck, notifyReady: () -> Unit) {
        notifyReady()
    }

    override fun getBackgroundWork(): BackgroundWork? {
        return null
    }
}