package ua.senko.remoteinput.utils

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import io.grpc.stub.StreamObserver
import ua.senko.remoteinput.data.ConnectionException
import ua.senko.remoteinput.data.Result
import java.lang.Exception
import kotlin.random.Random

class RemoteInputSender(context: Context) : RemoteInputChannel(context) {
    private var seed: Int = 0

    private val mutableOnConnect: MutableLiveData<Result> = MutableLiveData()
    val onConnect: LiveData<Result> = mutableOnConnect

    private val mutableNetworkState: MutableLiveData<Boolean> = MutableLiveData()
    val networkState: LiveData<Boolean> = Transformations.distinctUntilChanged(mutableNetworkState)

    private val connectObserver = object : StreamObserver<ConnectResponseMsg> {
        override fun onNext(value: ConnectResponseMsg?) {
            value?.let {
                if (it.check == seed) {
                    mutableOnConnect.postValue(Result.Success)
                } else {
                    mutableOnConnect.postValue(Result.Error(ConnectionException("Wrong seed")))
                }
            }
        }

        override fun onError(t: Throwable?) =
            mutableOnConnect.postValue(Result.Error(Exception("Failed to connect to the server", t)))

        override fun onCompleted() {}
    }

    private val emptyObserver = object : StreamObserver<Empty> {
        override fun onError(t: Throwable) = mutableNetworkState.postValue(false)
        override fun onCompleted() = mutableNetworkState.postValue(true)
        override fun onNext(value: Empty?) = mutableNetworkState.postValue(true)
    }

    override fun connect(host: String, port: Int) {
        super.connect(host, port)

        seed = Random.nextInt()
        asyncStub?.sendConnectData(
            ConnectDataMsg.newBuilder()
                .setCheck(seed)
                .build(),
            connectObserver
        )
    }

    fun sendMouseMove(msg: MouseDataMsg) {
        asyncStub?.sendMouseData(msg, emptyObserver)
    }

    fun sendButtonPress(pressed: Boolean, id: Int) {
        asyncStub?.sendButtonData(
            ButtonDataMsg.newBuilder()
                .setButton(id)
                .setPressed(pressed)
                .build(),
            emptyObserver
        )
    }

    fun sendScroll(value: Int) {
        asyncStub?.sendScrollData(
            ScrollDataMsg.newBuilder()
                .setValueX(0)
                .setValueY(value)
                .build(),
            emptyObserver
        )
    }
}