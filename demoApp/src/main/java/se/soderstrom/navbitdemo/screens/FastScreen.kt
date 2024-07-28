package se.soderstrom.navbitdemo.screens

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import com.google.android.material.appbar.MaterialToolbar
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

class FastScreen(context : Context) : Screen<ScreenData.Fast>(context) {

    override fun getLayoutIds(type: ScreenType): ScreenLayoutIds {
        return ScreenLayoutIds(R.layout.screen_fast)
    }

    override fun prepareLayout(view: View, type: ScreenType, onPrepared: (ScreenInsets) -> Unit) {
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setOnClickListener{
            EventBus.getDefault().post(Interaction.Back)
        }

        onPrepared(ScreenInsets(toolbar, null))
    }

    override fun entering(data: ScreenData.Fast, notifyReady: () -> Unit) {
        notifyReady()
    }

    override fun updating(oldData: ScreenData.Fast, data: ScreenData.Fast) {}

    override fun returning(oldData: ScreenData.Fast?, data: ScreenData.Fast, notifyReady: () -> Unit) {
        notifyReady()
    }

    override fun getBackgroundWork(): BackgroundWork? { return null }
}