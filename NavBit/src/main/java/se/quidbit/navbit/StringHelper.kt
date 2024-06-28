package se.quidbit.navbit

object StringHelper {
    //Pretty prints sealed class
    fun prettyPrintSealed(string: String) : String {
        return string.substringAfterLast(".").substringBefore("@").substringAfter("$").replace("$", ".")
    }
}