package ua.senko.remoteinput.ui.manipulator

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.SeekBar
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.manipulator_fragment.*
import kotlinx.android.synthetic.main.manipulator_fragment.view.*
import ua.senko.remoteinput.R
import ua.senko.remoteinput.data.Buttons
import ua.senko.remoteinput.data.Result

class ManipulatorFragment : Fragment()
{

    companion object {
        const val TAG = "ManipulatorFragment"

        fun newInstance() = ManipulatorFragment()
    }

    private lateinit var gestureDetector: GestureDetectorCompat
    private val viewModel: ManipulatorViewModel by activityViewModels()

    private var fabDefaultTintList: ColorStateList? = null

    private val middleButtonGestureListener = object : GestureDetector.OnGestureListener {
        override fun onShowPress(event: MotionEvent) {
        }

        override fun onSingleTapUp(event: MotionEvent): Boolean {
            //viewModel.setIsButtonPressed(false, Buttons.MOUSE_MIDDLE)
            return false
        }

        override fun onDown(event: MotionEvent): Boolean {
            //viewModel.setIsButtonPressed(true, Buttons.MOUSE_MIDDLE)
            return false
        }

        override fun onFling(event1: MotionEvent, event2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            return true
        }

        override fun onScroll(event1: MotionEvent, event2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            viewModel.scroll(distanceY)
            return true
        }

        override fun onLongPress(event: MotionEvent) {
        }
    }

    private val seekBarHandler = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(bar: SeekBar, progress: Int, fromUser: Boolean) {
            if (!fromUser) {
                return
            }
            when (bar) {
                mouseSensitivityBar -> viewModel.updateMouseSensitivity(progress + 1f)
                scrollSensitivityBar -> viewModel.updateScrollSensitivity(progress / 10f)
            }
        }
        override fun onStartTrackingTouch(bar: SeekBar) {}
        override fun onStopTrackingTouch(bar: SeekBar) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.manipulator_fragment, container, false).also {
            initView(it)
            initObservers(it)

            viewModel.connect()
        }
    }

    private fun initView(it: View) {
        fabDefaultTintList = it.adjustPosFab.backgroundTintList
        gestureDetector = GestureDetectorCompat(this.context, middleButtonGestureListener)

        it.retryButton.setOnClickListener {
            findNavController().navigate(R.id.action_manipulatorFragment_to_loginFragment)
        }

        it.adjustPosFab.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                viewModel.setInAdjustmentMode(true)
            } else if (motionEvent.action == MotionEvent.ACTION_UP) {
                viewModel.setInAdjustmentMode(false)
            }
            false
        }
        it.adjustPosFab.setOnClickListener {
            viewModel.registerAdjustClick()
        }

        it.mouseSensitivityBar.setOnSeekBarChangeListener(seekBarHandler)
        it.scrollSensitivityBar.setOnSeekBarChangeListener(seekBarHandler)

        it.leftMouseButton.setOnTouchListener(this::handleMouseButtonTouch)
        it.rightMouseButton.setOnTouchListener(this::handleMouseButtonTouch)
        it.scrollButton.setOnTouchListener { _, motionEvent -> gestureDetector.onTouchEvent(motionEvent) }
    }

    private fun initObservers(view: View) {
        viewModel.connectStatus.observe(viewLifecycleOwner, Observer(this::handleConnect))

        viewModel.sensorsInitStatus.observe(viewLifecycleOwner, Observer(this::handleInitSensors))

        viewModel.mouseSensitivity.observe(viewLifecycleOwner, Observer {
            mouseSensitivityBar.progress = it.toInt() - 1
        })

        viewModel.scrollSensitivity.observe(viewLifecycleOwner, Observer {
            scrollSensitivityBar.progress = (it * 10).toInt()
        })

        viewModel.loginRequiredEvent.observe(viewLifecycleOwner, Observer {
            findNavController().navigate(R.id.action_manipulatorFragment_to_loginFragment)
        })

        viewModel.gyroscopeData.observe(viewLifecycleOwner, Observer {
            gyroDataText.text = "x: ${it.deltaX}; y: ${it.deltaX}; z: ${it.deltaZ}"
        })

        viewModel.mouseEnabled.observe(viewLifecycleOwner, Observer { enabled ->
            if (enabled) {
                view.adjustPosFab.backgroundTintList = fabDefaultTintList
            } else {
                view.adjustPosFab.backgroundTintList = ColorStateList.valueOf(Color.LTGRAY)
            }
        })
    }

    private fun handleMouseButtonTouch(view: View, motionEvent: MotionEvent): Boolean {
        val pressed = when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> true
            MotionEvent.ACTION_UP -> false
            else -> return false
        }

        if (view.id == R.id.leftMouseButton) {
            viewModel.setIsButtonPressed(pressed, Buttons.MOUSE_LEFT)
        } else if (view.id == R.id.rightMouseButton) {
            viewModel.setIsButtonPressed(pressed, Buttons.MOUSE_RIGHT)
        }

        return false
    }

    private fun handleInitSensors(status: Result) {
        if (status is Result.Error) {
            showError(true, status.exception.message)
        } else {
            showError(false)
        }
    }

    private fun handleConnect(status: Result) {
        if (status is Result.Error) {
            Log.e(TAG, status.exception.message)
            status.exception.printStackTrace()
            showError(true, status.exception.message)
        } else {
            showError(false)
            viewModel.initSensors()
        }
    }

    private fun showError(show: Boolean, message: String? = null) {
        if (show) {
            manipulatorLayout.visibility = View.GONE
            errorLayout.visibility = View.VISIBLE
            errorText.text = message
        } else {
            manipulatorLayout.visibility = View.VISIBLE
            errorLayout.visibility = View.GONE
            errorText.text = null
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }
}
