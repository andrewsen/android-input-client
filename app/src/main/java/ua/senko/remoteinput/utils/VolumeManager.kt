package ua.senko.remoteinput.utils

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import android.provider.Settings
import android.util.Log

class VolumeManager(val context: Context) {
    companion object {
        const val TAG = "VolumeManager"
    }

    private val observer = VolumeContentObserver(Handler())
    private var volumeChanged: (Int) -> Unit = {}

    fun register() {
        observer.prepare()
        context.applicationContext.contentResolver.registerContentObserver(
            Settings.System.CONTENT_URI, true,
            observer
        )

        Log.i(TAG, "Prepared and registered")
    }

    fun unregister() {
        context.applicationContext.contentResolver.unregisterContentObserver(observer)
        Log.i(TAG, "Unregistered")
    }

    fun setOnVolumeChangedListener(listener: (Int) -> Unit) {
        volumeChanged = listener
    }

    inner class VolumeContentObserver(handler: Handler?) :
        ContentObserver(handler) {

        private var previousVolume = 0

        fun prepare() {
            val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        }

        override fun deliverSelfNotifications(): Boolean {
            return super.deliverSelfNotifications()
        }

        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)

            val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)

            Log.d(TAG, "Volume changed. Was $previousVolume, now $currentVolume")

            val delta = previousVolume - currentVolume

            if (delta > 0) {
                previousVolume = currentVolume
            } else if (delta < 0) {
                previousVolume = currentVolume
            }

            volumeChanged(delta)
        }
    }
}