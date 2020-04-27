package ua.senko.remoteinput.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.grpc.ManagedChannel
import io.grpc.android.AndroidChannelBuilder
import io.grpc.okhttp.OkHttpChannelBuilder
import io.grpc.stub.StreamObserver
import ua.senko.remoteinput.helpers.SingleLiveEvent

abstract class RemoteInputChannel(private val context: Context) {
    companion object {
        const val TAG = "RemoteInputChannel"
    }

    protected lateinit var messageChannel: ManagedChannel
    protected var asyncStub: RemoteInputServiceGrpc.RemoteInputServiceStub? = null

    open fun connect(host: String, port: Int) {
        messageChannel = AndroidChannelBuilder
            .usingBuilder(
                OkHttpChannelBuilder.forAddress(host, port)
            )
            .usePlaintext()
            .context(context.applicationContext)
            .build()

        asyncStub =
            RemoteInputServiceGrpc.newStub(
                messageChannel
            )
        Log.i(TAG, "Connected to gRPC server")
    }

    fun disconnect() {
        if (this::messageChannel.isInitialized) {
            asyncStub = null
            messageChannel.shutdown()
        }
    }
}

