package se.quidbit.navbit.types

import se.quidbit.navbit.toimplement.NavBitScreenData

sealed class ScreenDataResult<D : NavBitScreenData> {
    data class ErrorRead<D : NavBitScreenData>(val error : ReadError) : ScreenDataResult<D>()
    data class Success<D : NavBitScreenData>(val data : D, val type : ScreenType) : ScreenDataResult<D>()
}