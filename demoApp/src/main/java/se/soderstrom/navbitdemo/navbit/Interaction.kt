package se.soderstrom.navbitdemo.navbit

import se.quidbit.navbit.NavBitInteraction

sealed class Interaction : NavBitInteraction() {
    class Back : Interaction()
    class ViewInfo  : Interaction()
    class ViewInfoDetails  : Interaction()
    class Done  : Interaction()
}