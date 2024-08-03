package se.soderstrom.navbitdemo.screens

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView
import org.greenrobot.eventbus.EventBus
import se.quidbit.navbit.types.BackgroundWork
import se.quidbit.navbit.toimplement.NavBitScreen
import se.quidbit.navbit.types.ScreenInsets
import se.quidbit.navbit.types.ScreenLayoutIds
import se.quidbit.navbit.types.ScreenType
import se.soderstrom.navbitdemo.R
import se.soderstrom.navbitdemo.navbit.Interaction
import se.soderstrom.navbitdemo.navbit.ScreenData

class SheetsInfoScreen(context : Context) : NavBitScreen<ScreenData.Info>(context) {

    override fun getLayoutIds(type: ScreenType): ScreenLayoutIds {
        return ScreenLayoutIds(R.layout.screen_info)
    }

    override fun prepareLayout(view: View, type: ScreenType, onPrepared: (ScreenInsets) -> Unit) {
        val detailsButton = view.findViewById<MaterialCardView>(R.id.details_button)
        detailsButton.setOnClickListener{
            EventBus.getDefault().post(Interaction.ViewSheetsInfoDetails)
        }

        val doneButton = view.findViewById<MaterialCardView>(R.id.done_button)
        doneButton.setOnClickListener{
            EventBus.getDefault().post(Interaction.Back)
        }

        val mainContent = view.findViewById<LinearLayout>(R.id.main_content)
        onPrepared(ScreenInsets(mainContent))
    }

    override fun entering(data: ScreenData.Info, notifyDone: () -> Unit) {
        notifyDone()
    }

    override fun updating(data: ScreenData.Info) {}

    override fun returning(data: ScreenData.Info, notifyDone: () -> Unit) {
        notifyDone()
    }

    override fun getBackgroundWork(): BackgroundWork? {
        return null
    }
}