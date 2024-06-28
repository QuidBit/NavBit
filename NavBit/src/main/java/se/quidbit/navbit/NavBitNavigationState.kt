package se.quidbit.navbit

abstract class NavBitNavigationState {
    open fun prettyString() : String {
        return "State: [${StringHelper.prettyPrintSealed(toString())}]"
    }

    abstract fun deepCopy() : NavBitNavigationState
}