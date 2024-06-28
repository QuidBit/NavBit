package se.quidbit.navbit

interface NavBitScreenData {
    fun tag() : String {
        return javaClass.toString()
    }
}