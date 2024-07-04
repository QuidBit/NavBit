package se.soderstrom.navbitdemo.screens

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
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

class StartScreen(context : Context) : Screen<ScreenData.Start>(context) {

    private lateinit var incrementText : TextView

    override fun getLayoutIds(type: ScreenType): ScreenLayoutIds {
        return ScreenLayoutIds(R.layout.screen_start)
    }

    override fun prepareLayout(view: View, type: ScreenType): ScreenInsets {
        val infoButton = view.findViewById<MaterialCardView>(R.id.info_button)
        infoButton.setOnClickListener{
            EventBus.getDefault().post(Interaction.ViewSheetsInfo)
        }

        incrementText = view.findViewById(R.id.increment_text)

        val incrementButton = view.findViewById<MaterialCardView>(R.id.increment_button)
        incrementButton.setOnClickListener {
            EventBus.getDefault().post(Interaction.Increment)
        }

        val clearButton = view.findViewById<MaterialCardView>(R.id.clear_button)
        clearButton.setOnClickListener {
            EventBus.getDefault().post(Interaction.ClearCheck)
        }

        return ScreenInsets()
    }

    override fun entering(data: ScreenData.Start, notifyReady: () -> Unit) {
        updateData(data)
        notifyReady()
    }

    override fun updating(oldData: ScreenData.Start, data: ScreenData.Start) {
        updateData(data)
    }

    override fun returning(oldData: ScreenData.Start?, data: ScreenData.Start, notifyReady: () -> Unit) {
        updateData(data)
        notifyReady()
    }

    override fun getBackgroundWork(): BackgroundWork? {
        return null
    }

    @SuppressLint("SetTextI18n")
    private fun updateData(data : ScreenData.Start) {
        incrementText.text = "Count: ${data.count}"
    }
}