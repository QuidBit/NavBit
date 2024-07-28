package se.soderstrom.navbitdemo.screens

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
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

class SheetsInfoDetailsScreen(context : Context) : Screen<ScreenData.InfoDetails>(context) {

    lateinit var expandButtonText : TextView
    lateinit var expandText : TextView

    override fun getLayoutIds(type: ScreenType): ScreenLayoutIds {
        return ScreenLayoutIds(R.layout.screen_info_details)
    }

    override fun prepareLayout(view: View, type: ScreenType, onPrepared: (ScreenInsets) -> Unit) {
        val doneButton = view.findViewById<MaterialCardView>(R.id.done_button)
        doneButton.setOnClickListener{
            EventBus.getDefault().post(Interaction.Done)
        }

        expandButtonText = view.findViewById(R.id.expand_button_text)
        expandText = view.findViewById(R.id.expand_text)
        val expandButton = view.findViewById<MaterialCardView>(R.id.expand_button)
        expandButton?.setOnClickListener{
            EventBus.getDefault().post(Interaction.Expand)
        }

        val mainContent = view.findViewById<LinearLayout>(R.id.main_content)
        onPrepared(ScreenInsets(mainContent))
    }

    override fun entering(data: ScreenData.InfoDetails, notifyReady: () -> Unit) {
        updateData(data)
        notifyReady()
    }

    override fun updating(oldData: ScreenData.InfoDetails, data: ScreenData.InfoDetails) {
        updateData(data)
    }

    override fun returning(oldData: ScreenData.InfoDetails?, data: ScreenData.InfoDetails, notifyReady: () -> Unit) {
        notifyReady()
    }

    override fun getBackgroundWork(): BackgroundWork? {
        return null
    }

    private fun updateData(data : ScreenData.InfoDetails) {
        expandButtonText.text = when (data.expanded) {
            false -> "Expand"
            true -> "Collapse"
        }
        expandText.visibility = when (data.expanded) {
            false -> View.GONE
            true -> View.VISIBLE
        }
    }
}