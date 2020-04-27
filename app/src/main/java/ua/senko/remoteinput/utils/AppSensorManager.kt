package ua.senko.remoteinput.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ua.senko.remoteinput.data.GyroData
import ua.senko.remoteinput.data.Result
import ua.senko.remoteinput.data.asResult
import java.lang.Exception
import java.util.concurrent.atomic.AtomicBoolean

class AppSensorManager(private val context: Context): SensorEventListener {
    companion object {
        const val TAG = "AppSensorManager"
    }

    private lateinit var gyroscope: Sensor
    private lateinit var sensorManager: SensorManager

    private val started = AtomicBoolean(false)

    private val mutableGyroscopeData: MutableLiveData<GyroData> = MutableLiveData()
    val gyroscopeData: LiveData<GyroData> = mutableGyroscopeData

    fun init(): Result {
        sensorManager = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            ?: return Result.Error(Exception("Can't access gyroscope"))

        return Result.Success
    }

    fun start() {
        if (started.compareAndSet(false, true)) {
            Log.i(TAG, "Started listening to sensors")
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        if (started.compareAndSet(true, false)) {
            Log.i(TAG, "Stopped listening to sensors")
            sensorManager.unregisterListener(this)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) {
            return
        }

        val gyro = GyroData(event.values[0], event.values[1], event.values[2])

        mutableGyroscopeData.postValue(gyro)
    }
}
