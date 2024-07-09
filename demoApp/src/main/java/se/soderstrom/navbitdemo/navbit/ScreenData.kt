package se.soderstrom.navbitdemo.navbit

import se.quidbit.navbit.NavBitScreenData

sealed class ScreenData : NavBitScreenData {
    class Start(var count : Int) : ScreenData()
    object ClearCheck : ScreenData()
    object Info : ScreenData()
    object InfoDetails : ScreenData()
    object Timer : ScreenData()
}