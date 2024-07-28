package se.quidbit.navbit.internal

internal object StringHelper {
    fun prettyPrintSealedClassString(string: String) : String {
        return string.substringAfterLast(".").substringBefore("@").substringAfter("$").replace("$", ".")
    }
}