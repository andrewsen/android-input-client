package ua.senko.remoteinput.data

import java.lang.Exception

sealed class Resource<out R> {

    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val exception: Exception) : Resource<Nothing>()
    object Loading : Resource<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
            Loading -> "Loading"
        }
    }
}

val Resource<*>.succeeded
    get() = this is Resource.Success && data != null


fun Resource<*>.asResult(): Result = when (this) {
    is Resource.Success<*> -> Result.Success
    is Resource.Error -> Result.Error(exception)
    Resource.Loading -> Result.Loading
}