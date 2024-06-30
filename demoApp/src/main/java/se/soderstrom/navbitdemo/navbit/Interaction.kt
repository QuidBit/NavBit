package se.soderstrom.navbitdemo.navbit

import se.quidbit.navbit.NavBitInteraction

sealed class Interaction : NavBitInteraction() {
    object Back : Interaction()
    object ViewSheetsInfo  : Interaction()
    object ViewSheetsInfoDetails  : Interaction()
    object Done  : Interaction()
    object Increment  : Interaction()
}