package ua.senko.remoteinput.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ua.senko.remoteinput.data.AppRepository
import ua.senko.remoteinput.data.Result
import java.lang.Exception

class LoginViewModel (app: Application) : AndroidViewModel(app) {
    private val appRepository = AppRepository.getInstance(app)

    private val mutableSaveStatus: MutableLiveData<Result> = MutableLiveData()
    private val mutableCachedAddress: MutableLiveData<String> = MutableLiveData()

    val addressSaveStatus: LiveData<Result>
        get() = mutableSaveStatus

    val cachedAddress: LiveData<String>
        get() = mutableCachedAddress

    init {
        appRepository.getAddress()?.let{
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
}
