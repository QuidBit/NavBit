package se.quidbit.navbit.toimplement

import se.quidbit.navbit.internal.StringHelper

abstract class NavBitNavigationState {
    open fun prettyString() : String {
        return "State: [${StringHelper.prettyPrintSealedClassString(toString())}]"
    }

    abstract fun deepCopy() : NavBitNavigationState
}