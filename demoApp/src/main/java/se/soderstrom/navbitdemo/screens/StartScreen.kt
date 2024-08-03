package se.soderstrom.navbitdemo.screens

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.TextView
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

class StartScreen(context : Context) : NavBitScreen<ScreenData.Start>(context) {

    private lateinit var incrementText : TextView
    private lateinit var clearButton : MaterialCardView

    override fun getLayoutIds(type: ScreenType): ScreenLayoutIds {
        return ScreenLayoutIds(R.layout.screen_start)
    }

    override fun prepareLayout(view: View, type: ScreenType, onPrepared: (ScreenInsets) -> Unit) {
        val infoButton = view.findViewById<MaterialCardView>(R.id.info_button)
        infoButton.setOnClickListener{
            EventBus.getDefault().post(Interaction.ViewSheetsInfo)
        }

        incrementText = view.findViewById(R.id.increment_text)

        val incrementButton = view.findViewById<MaterialCardView>(R.id.increment_button)
        incrementButton.setOnClickListener {
            EventBus.getDefault().post(Interaction.Increment)
        }

        clearButton = view.findViewById(R.id.clear_button)
        clearButton.setOnClickListener {
            EventBus.getDefault().post(Interaction.ClearCheck)
        }

        val timerButton = view.findViewById<MaterialCardView>(R.id.timer_button)
        timerButton.setOnClickListener {
            EventBus.getDefault().post(Interaction.GoToTimer)
        }

        val slowButton = view.findViewById<MaterialCardView>(R.id.slow_button)
        slowButton.setOnClickListener {
            EventBus.getDefault().post(Interaction.GoToSlow)
        }

        onPrepared(ScreenInsets())
    }

    override fun entering(data: ScreenData.Start, notifyDone: () -> Unit) {
        updateData(data)
        notifyDone()
    }

    override fun updating(data: ScreenData.Start) {
        updateData(data)
    }

    override fun returning(data: ScreenData.Start, notifyDone: () -> Unit) {
        updateData(data)
        notifyDone()
    }

    override fun getBackgroundWork(): BackgroundWork? {
        return null
    }

    @SuppressLint("SetTextI18n")
    private fun updateData(data : ScreenData.Start) {
        incrementText.text = "Count: ${data.count}"

        clearButton.alpha = when (data.count == 0) {
            true -> 0.5f
            false -> 1.0f
        }
    }
}