package se.quidbit.navbit

sealed class NavigationResult<T : NavBitScreenData> {
    class ErrorRead<T : NavBitScreenData>(var error : ReadError) : NavigationResult<T>()

    class Update<T : NavBitScreenData>(var data: T) : NavigationResult<T>()
    class Navigate<T : NavBitScreenData>(var type : ScreenType, var data  : T) : NavigationResult<T>()
}