package se.quidbit.navbit.toimplement

class NavBitUpdates<U> {
    private var updates : ArrayList<U> = ArrayList()

    constructor()

    constructor(vararg initialUpdates: U) {
        updates.addAll(initialUpdates)
    }

    fun add(update : U) : NavBitUpdates<U> {
        updates.add(update)
        return this
    }

    fun consume() : ArrayList<U> {
        val consumedUpdates = ArrayList(updates)
        updates.clear()
        return consumedUpdates
    }
}