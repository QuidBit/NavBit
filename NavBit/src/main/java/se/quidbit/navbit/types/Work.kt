package se.quidbit.navbit.types

data class BackgroundWork (
    val periodMs : Long? = null,
    val work : () -> UIwork
)

data class UIwork (
    val work : () -> Unit
)