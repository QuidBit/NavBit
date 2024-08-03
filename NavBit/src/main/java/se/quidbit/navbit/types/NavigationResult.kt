package se.quidbit.navbit.types

import se.quidbit.navbit.toimplement.NavBitScreenData

sealed class NavigationResult<D : NavBitScreenData> {
    class ErrorRead<D : NavBitScreenData>(var error : ReadError) : NavigationResult<D>()
    class Update<D : NavBitScreenData>(var data: D) : NavigationResult<D>()
    class Navigate<D : NavBitScreenData>(var type : ScreenType, var data  : D) : NavigationResult<D>()
}