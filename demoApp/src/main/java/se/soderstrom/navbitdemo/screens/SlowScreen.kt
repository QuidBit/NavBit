package se.soderstrom.navbitdemo.screens

import android.content.Context
import android.os.Handler
import android.os.Looper
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

class SlowScreen(context : Context) : Screen<ScreenData.Slow>(context) {

    override fun getLayoutIds(type: ScreenType): ScreenLayoutIds {
        return ScreenLayoutIds(R.layout.screen_slow)
    }

    override fun prepareLayout(view: View, type: ScreenType, onPrepared: (ScreenInsets) -> Unit) {
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setOnClickListener{
            EventBus.getDefault().post(Interaction.Back)
        }

        val anotherButton = view.findViewById<MaterialCardView>(R.id.another_button)
        anotherButton.setOnClickListener{
            EventBus.getDefault().post(Interaction.Expand)
        }

        onPrepared(ScreenInsets(toolbar, null))
    }

    override fun entering(data: ScreenData.Slow, notifyReady: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed({
            notifyReady()
        }, 2000)
    }

    override fun updating(oldData: ScreenData.Slow, data: ScreenData.Slow) {}

    override fun returning(oldData: ScreenData.Slow?, data: ScreenData.Slow, notifyReady: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed({
            notifyReady()
        }, 2000)
    }

    override fun getBackgroundWork(): BackgroundWork? { return null }
}