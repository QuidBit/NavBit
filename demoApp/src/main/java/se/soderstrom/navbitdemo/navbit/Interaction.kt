package se.soderstrom.navbitdemo.navbit

import se.quidbit.navbit.NavBitInteraction

sealed class Interaction : NavBitInteraction() {
    object Back : Interaction()
    object ClearCheck : Interaction()
    object ClearPerform : Interaction()
    object ViewSheetsInfo  : Interaction()
    object ViewSheetsInfoDetails  : Interaction()
    object GoToTimer  : Interaction()
    object Done  : Interaction()
    object Increment  : Interaction()
    object Expand  : Interaction()
}