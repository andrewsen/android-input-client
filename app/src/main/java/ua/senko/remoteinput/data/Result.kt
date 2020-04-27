package ua.senko.remoteinput.data

sealed class Result {
    data class Error(val exception: Exception) : Result()

    object Success : Result()
    object Loading : Result()

    override fun toString(): String {
        return when (this) {
            is Success -> "Success"
            is Error -> "Error[exception=$exception]"
            Loading -> "Loading"
        }
    }
}

val Result.succeeded
    get() = this is Result.Success

fun Result.asResource(): Resource<Boolean> = when (this) {
    Result.Success -> Resource.Success(true)
    Result.Loading -> Resource.Loading
    is Result.Error -> Resource.Error(exception)
}