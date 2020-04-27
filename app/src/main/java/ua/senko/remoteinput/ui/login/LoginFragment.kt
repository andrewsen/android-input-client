package ua.senko.remoteinput.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.login_fragment.*
import kotlinx.android.synthetic.main.login_fragment.view.*
import ua.senko.remoteinput.R
import ua.senko.remoteinput.data.Result


class LoginFragment : Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.login_fragment, container, false).also {
            viewModel.cachedAddress.observe(viewLifecycleOwner, Observer { address ->
                addressInputEditText.setText(address)
            })

            viewModel.addressSaveStatus.observe(viewLifecycleOwner, Observer(this::handleSaveStatus))

            it.connectButton.setOnClickListener {
                viewModel.saveAddress(addressInputEditText.text.toString())
            }
        }
    }

    private fun handleSaveStatus(connectResult: Result) {
        if (connectResult is Result.Error) {
            addressInputLayout.error = connectResult.exception.message
        } else {
            addressInputLayout.error = null
            findNavController().popBackStack()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }
}
