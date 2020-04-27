package ua.senko.remoteinput.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import io.grpc.stub.StreamObserver
import ua.senko.remoteinput.data.Buttons
import ua.senko.remoteinput.data.asResult
import ua.senko.remoteinput.data.Result
import ua.senko.remoteinput.data.alsoIf

class AppConnectionManager(private val context: Context) {
    companion object {
        const val TAG = "AppConnectionManager"
    }

    private val grpcSender = RemoteInputSender(context.applicationContext)

    val networkState: LiveData<Boolean> = grpcSender.networkState
    val connectState: LiveData<Result> = Transformations.map(grpcSender.onConnect) {
        it.alsoIf(it is Result.Error) {
            disconnect()
        }
    }

    fun connect(address: String) {
        if (address.contains(':')) {
            val (host, port) = address.split(':')
            grpcSender.connect(host, port.toInt())
        } else {
            grpcSender.connect(address, 8080)
        }
    }

    fun sendMouseMove(deltaX: Int, deltaY: Int) {
        Log.i(TAG, "Sending move: x: $deltaX, y: $deltaY")
        grpcSender.sendMouseMove(MouseDataMsg
            .newBuilder()
            .setDeltaX(deltaX)
            .setDeltaY(deltaY)
            .build()
        )
    }

    fun disconnect() {
        grpcSender.disconnect()
    }

    fun sendButtonPress(pressed: Boolean, button: Buttons) {
        grpcSender.sendButtonPress(pressed, button.id)
    }

    fun sendScroll(value: Int) {
        grpcSender.sendScroll(value)
    }
}
