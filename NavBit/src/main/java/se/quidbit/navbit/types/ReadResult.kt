package se.quidbit.navbit.types

sealed class ReadResult<T> {
    data class Success<T>(var data : T) : ReadResult<T>()
    data class Error<T>(var error : ReadError) : ReadResult<T>()
}

data class ReadError(var error : String)