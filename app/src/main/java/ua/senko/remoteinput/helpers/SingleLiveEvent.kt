package ua.senko.remoteinput.helpers

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import android.util.Log;
import androidx.annotation.MainThread

import java.util.concurrent.atomic.AtomicBoolean;


class SingleLiveEvent<T> : MutableLiveData<T>() {
    private val mPending = AtomicBoolean(false)

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (hasActiveObservers()) {
            Log.w(
                TAG,
                "Multiple observers registered but only one will be notified of changes."
            )
        }

        // Observe the internal MutableLiveData
        super.observe(owner, Observer {
            if (mPending.compareAndSet(true, false)) {
                observer.onChanged(it)
            }
        })
    }

    override fun postValue(t: T?) {
        mPending.set(true)
        super.postValue(t)
    }

    fun call() {
        postValue(null)
    }

    companion object {
        private const val TAG = "SingleLiveEvent"
    }
}