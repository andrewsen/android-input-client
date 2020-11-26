package ua.senko.remoteinput.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ua.senko.remoteinput.data.AppRepository
import ua.senko.remoteinput.data.Result
import java.lang.Exception

class LoginViewModel (app: Application) : AndroidViewModel(app) {
    companion object {
        private const val CACHE_ADDRESS = "address"
    }

    private val appRepository = AppRepository.getInstance(app)

    private val mutableSaveStatus: MutableLiveData<Result> = MutableLiveData()
    private val mutableCachedAddress: MutableLiveData<String> = MutableLiveData()

    val addressSaveStatus: LiveData<Result> = mutableSaveStatus
    val cachedAddress: LiveData<String> = mutableCachedAddress

    init {
        appRepository.getCacheItem(CACHE_ADDRESS)?.let{
            mutableCachedAddress.postValue(it)
        }
    }

    fun saveAddress(address: String) {
        if (address.isBlank()) {
            mutableSaveStatus.postValue(
                Result.Error(Exception("Address should not be null"))
            )
        } else {
            mutableSaveStatus.postValue(Result.Success)
        }

        appRepository.saveAddress(address)
    }

    fun cacheAddress(address: String) {
        appRepository.putCacheItem(CACHE_ADDRESS, address)
    }
}
