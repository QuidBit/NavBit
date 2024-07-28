package se.quidbit.navbit.toimplement

interface NavBitScreenData {
    fun tag() : String {
        return javaClass.toString()
    }
}