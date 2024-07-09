package se.soderstrom.navbitdemo.screens

import android.content.Context
import android.view.View
import android.widget.TextView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import org.greenrobot.eventbus.EventBus
import se.quidbit.navbit.BackgroundWork
import se.quidbit.navbit.Screen
import se.quidbit.navbit.ScreenInsets
import se.quidbit.navbit.ScreenLayoutIds
import se.quidbit.navbit.ScreenType
import se.quidbit.navbit.UIwork
import se.soderstrom.navbitdemo.R
import se.soderstrom.navbitdemo.navbit.Interaction
import se.soderstrom.navbitdemo.navbit.ScreenData

class TimerScreen(context : Context) : Screen<ScreenData.Timer>(context) {

    private var seconds = 0
    private lateinit var timeText : TextView

    override fun getLayoutIds(type: ScreenType): ScreenLayoutIds {
        return ScreenLayoutIds(R.layout.screen_timer)
    }

    override fun prepareLayout(view: View, type: ScreenType): ScreenInsets {
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setOnClickListener{
            EventBus.getDefault().post(Interaction.Back)
        }

        timeText = view.findViewById(R.id.timeText)

        return ScreenInsets(toolbar, null)
    }

    override fun entering(data: ScreenData.Timer, notifyReady: () -> Unit) {
        notifyReady()
    }

    override fun updating(oldData: ScreenData.Timer, data: ScreenData.Timer) {}

    override fun returning(oldData: ScreenData.Timer?, data: ScreenData.Timer, notifyReady: () -> Unit) {
        notifyReady()
    }

    override fun getBackgroundWork(): BackgroundWork? {
        return BackgroundWork(1000) {
            // NOTE: For demonstrational purposes only, subject to drifting due to non-exact scheduling
            UIwork {
                updateTimeDisplay()
                seconds += 1
            }
        }
    }

    private fun updateTimeDisplay() {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        timeText.text = String.format("%02d:%02d:%02d", hours, minutes, secs)
    }
}