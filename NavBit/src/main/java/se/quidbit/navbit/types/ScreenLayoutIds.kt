package se.quidbit.navbit.types

data class ScreenLayoutIds (
    val full : Int?,
    val sheet : Int?,
    val popup : Int?
) {
    constructor(id: Int) : this(id, id, id)
}