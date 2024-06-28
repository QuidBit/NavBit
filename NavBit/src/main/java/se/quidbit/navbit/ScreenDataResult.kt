package se.quidbit.navbit

sealed class ScreenDataResult<T : NavBitScreenData> {
    data class ErrorRead<T : NavBitScreenData>(val error : ReadError) : ScreenDataResult<T>()
    data class Success<T : NavBitScreenData>(val data : T, val type : ScreenType) : ScreenDataResult<T>()
}