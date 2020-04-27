package ua.senko.remoteinput.ui.manipulator

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.senko.remoteinput.data.AppRepository
import ua.senko.remoteinput.data.Buttons
import ua.senko.remoteinput.data.GyroData
import ua.senko.remoteinput.data.Result
import ua.senko.remoteinput.helpers.SingleLiveEvent
import ua.senko.remoteinput.utils.AppConnectionManager
import ua.senko.remoteinput.utils.AppSensorManager
import kotlin.math.roundToInt

class ManipulatorViewModel(private val app: Application) : AndroidViewModel(app) {
    companion object {
        const val TAG = "ManipulatorViewModel"
        const val DOUBLE_CLICK_DELAY = 250L
    }

    private val sensorManager = AppSensorManager(app)
    private val connectionManager = AppConnectionManager(app)
    private val repository = AppRepository.getInstance(app)

    private var prevGyroData = GyroData(0F, 0F, 0F)
    private var lastAdjustClick = 0L

    val connectStatus: LiveData<Result> = connectionManager.connectState

    private val mutableLoginRequiredEvent: SingleLiveEvent<Unit> = SingleLiveEvent()
    val loginRequiredEvent: LiveData<Unit> = mutableLoginRequiredEvent

    private val mutableSensorsInitStatus: MutableLiveData<Result> = MutableLiveData()
    val sensorsInitStatus: LiveData<Result> = mutableSensorsInitStatus

    private val mutableMouseSensitivity: MutableLiveData<Float> = MutableLiveData(repository.getMouseSensitivity())
    val mouseSensitivity: LiveData<Float> = mutableMouseSensitivity

    private val mutableScrollSensitivity: MutableLiveData<Float> = MutableLiveData(repository.getScrollSensitivity())
    val scrollSensitivity: LiveData<Float> = mutableScrollSensitivity

    private val mutableMouseEnabled: MutableLiveData<Boolean> = MutableLiveData(true)
    val mouseEnabled: LiveData<Boolean> = mutableMouseEnabled

    val gyroscopeData: LiveData<GyroData> = Transformations.distinctUntilChanged(Transformations.map(sensorManager.gyroscopeData) {
        scaleAndSendGyroData(it)
    })

    override fun onCleared() {
        sensorManager.stop()
    }

    fun connect() = viewModelScope.launch {
        connectAsync()
    }

    fun initSensors() {
        sensorManager.init().also {
            if (it is Result.Success && mouseEnabled.value == true) {
                sensorManager.start()
            }
            mutableSensorsInitStatus.postValue(it)
        }
    }

    fun setInAdjustmentMode(enabled: Boolean) {
        if (!mouseEnabled.value!!) {
            return
        }

        if (enabled) {
            sensorManager.stop()
        } else {
            sensorManager.start()
        }
    }

    fun setIsButtonPressed(pressed: Boolean, button: Buttons) {
        connectionManager.sendButtonPress(pressed, button)
    }

    fun updateMouseSensitivity(progress: Float) {
        repository.setMouseSensitivity(progress)
        mutableMouseSensitivity.value = repository.getMouseSensitivity()
    }

    fun updateScrollSensitivity(progress: Float) {
        repository.setScrollSensitivity(progress)
        mutableScrollSensitivity.value = repository.getScrollSensitivity()
    }

    private fun scaleAndSendGyroData(data: GyroData): GyroData {
        val sensitivity = mouseSensitivity.value!!

        val scaledData = data.copy(
            deltaX = (sensitivity * data.deltaX).roundToInt().toFloat(),
            deltaY = (sensitivity * data.deltaY).roundToInt().toFloat(),
            deltaZ = (sensitivity * data.deltaZ).roundToInt().toFloat()
        )

        if (scaledData != prevGyroData) {
            val (dx, dy) = gyroscopeDataToMovement(scaledData)

            connectionManager.sendMouseMove(dx, dy)

            prevGyroData = scaledData
        }

        return scaledData
    }

    private fun gyroscopeDataToMovement(data: GyroData): IntArray {
        return intArrayOf(-data.deltaZ.toInt(), -data.deltaX.toInt())
    }

    private suspend fun connectAsync() {
        withContext(Dispatchers.IO) {
            val address = repository.getAddress()
            if (address == null) {
                mutableLoginRequiredEvent.call()
                return@withContext
            }

            connectionManager.connect(address)
        }
    }

    fun registerAdjustClick() {
        val time = SystemClock.elapsedRealtime()

        if (time - lastAdjustClick < DOUBLE_CLICK_DELAY) {
            val isMouseEnabled = !mouseEnabled.value!!

            if (isMouseEnabled) {
                sensorManager.start()
            } else {
                sensorManager.stop()
            }

            mutableMouseEnabled.postValue(isMouseEnabled)
        }

        lastAdjustClick = time
    }

    fun scroll(distanceY: Float) {
        val sensitivity = scrollSensitivity.value!!

        connectionManager.sendScroll((sensitivity * distanceY).toInt())
    }
}
