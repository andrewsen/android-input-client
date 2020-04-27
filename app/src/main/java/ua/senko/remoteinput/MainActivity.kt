package ua.senko.remoteinput

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.viewModels
import ua.senko.remoteinput.data.Buttons
import ua.senko.remoteinput.ui.manipulator.ManipulatorFragment
import ua.senko.remoteinput.ui.manipulator.ManipulatorViewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: ManipulatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                viewModel.setIsButtonPressed(false, Buttons.VOLUME_DOWN)
                true
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                viewModel.setIsButtonPressed(false, Buttons.VOLUME_UP)
                true
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                viewModel.setIsButtonPressed(true, Buttons.VOLUME_DOWN)
                true
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                viewModel.setIsButtonPressed(true, Buttons.VOLUME_UP)
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}
