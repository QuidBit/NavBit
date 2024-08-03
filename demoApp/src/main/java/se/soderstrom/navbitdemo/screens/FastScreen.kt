package se.soderstrom.navbitdemo.screens

import android.content.Context
import android.view.View
import com.google.android.material.appbar.MaterialToolbar
import org.greenrobot.eventbus.EventBus
import se.quidbit.navbit.types.BackgroundWork
import se.quidbit.navbit.types.ScreenLayoutIds
import se.quidbit.navbit.toimplement.NavBitScreen
import se.quidbit.navbit.types.ScreenInsets
import se.quidbit.navbit.types.ScreenType
import se.soderstrom.navbitdemo.R
import se.soderstrom.navbitdemo.navbit.Interaction
import se.soderstrom.navbitdemo.navbit.ScreenData

class FastScreen(context : Context) : NavBitScreen<ScreenData.Fast>(context) {

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

    override fun entering(data: ScreenData.Fast, notifyDone: () -> Unit) {
        notifyDone()
    }

    override fun updating(data: ScreenData.Fast) {}

    override fun returning(data: ScreenData.Fast, notifyDone: () -> Unit) {
        notifyDone()
    }

    override fun getBackgroundWork(): BackgroundWork? { return null }
}