package se.quidbit.navbit.toimplement

import se.quidbit.navbit.internal.StringHelper

abstract class NavBitNavigationState {
    fun prettyString() : String {
        return "State: [${StringHelper.prettyPrintSealedClassString(toString())}]"
    }
}