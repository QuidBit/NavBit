package se.soderstrom.navbitdemo.navbit

import se.quidbit.navbit.toimplement.NavBitInteraction

sealed class Interaction : NavBitInteraction() {
    data object ClearCheck : Interaction()
    data object ClearPerform : Interaction()
    data object ViewSheetsInfo  : Interaction()
    data object ViewSheetsInfoDetails  : Interaction()
    data object GoToNext  : Interaction()
    data object Done  : Interaction()
    data object Increment  : Interaction()
    data object Expand  : Interaction()
}