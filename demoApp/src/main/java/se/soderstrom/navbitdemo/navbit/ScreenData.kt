package se.soderstrom.navbitdemo.navbit

import se.quidbit.navbit.NavBitScreenData

sealed class ScreenData : NavBitScreenData {
    object Start : ScreenData()
    object Info : ScreenData()
    object InfoDetails : ScreenData()
}