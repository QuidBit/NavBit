package se.soderstrom.navbitdemo.newnavbit

import android.content.Context
import se.quidbit.navbit.updated.toimplement.NewBitScreenHandler
import se.quidbit.navbit.updated.types.InteractionReceiver
import se.quidbit.navbit.updated.types.ScreenArrangement
import se.soderstrom.navbitdemo.newscreen.PopupClearScreen
import se.soderstrom.navbitdemo.newscreen.SheetsInfoDetailsScreen
import se.soderstrom.navbitdemo.newscreen.StartScreen
import se.soderstrom.navbitdemo.newscreen.SheetsInfoScreen

class NewScreenHandler : NewBitScreenHandler<Interaction, NavigationState>() {
    override fun screenArrangementFromNavigationState(
        s: NavigationState,
        i: InteractionReceiver<Interaction>,
        context: Context
    ): ScreenArrangement {
        return when (s) {
            is NavigationState.Start -> ScreenArrangement.main("StartScreen") {
                StartScreen(i, s.count)
            }
            is NavigationState.ClearCheck -> ScreenArrangement.main("PopupClearScreen") {
                PopupClearScreen({i.send(Interaction.ClearPerform)}, {i.send(Interaction.Back)})
            }
            is NavigationState.Fast -> TODO()
            is NavigationState.Info -> ScreenArrangement.main("SheetsInfoScreen") {
                SheetsInfoScreen({ }, { i.send(Interaction.Back)})
            }
            is NavigationState.InfoDetails -> ScreenArrangement.main("SheetsInfoDetailScreen") {
                SheetsInfoDetailsScreen({}, {}, s.expanded)
            }
            is NavigationState.Slow -> TODO()
            is NavigationState.Timer -> TODO()
        }
    }
}