package se.soderstrom.navbitdemo.newnavbit

import se.quidbit.navbit.toimplement.NavBitInteraction

sealed class Interaction : NavBitInteraction() {
    data object Back : Interaction()
    data object ClearCheck : Interaction()
    data object ClearPerform : Interaction()
    data object ViewSheetsInfo  : Interaction()
    data object ViewSheetsInfoDetails  : Interaction()
    data object GoToTimer  : Interaction()
    data object GoToSlow  : Interaction()
    data object Done  : Interaction()
    data object Increment  : Interaction()
    data object Expand  : Interaction()
}