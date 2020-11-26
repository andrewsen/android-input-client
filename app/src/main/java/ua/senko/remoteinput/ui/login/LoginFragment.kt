package ua.senko.remoteinput.ui.login

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.login_fragment.*
import kotlinx.android.synthetic.main.login_fragment.view.*
import kotlinx.coroutines.launch
import ua.senko.remoteinput.R
import ua.senko.remoteinput.data.Result
import ua.senko.remoteinput.data.Server


class LoginFragment : Fragment() {
    companion object {
        const val TAG = "LoginFragment"
        const val SERVICE_NAME = "Input Server"
        const val SERVICE_TYPE = "_grpc._tcp."
    }

    private lateinit var rootView: View
    private val viewModel: LoginViewModel by viewModels()
    private val servers: MutableList<Server> = mutableListOf()

    private lateinit var serverListAdapter: ServerListAdapter
    private lateinit var nsdManager: NsdManager

    private val discoveryListener: NsdManager.DiscoveryListener = object : NsdManager.DiscoveryListener {
        // Called as soon as service discovery begins.
        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "Service discovery started")
            rootView.nsdScanProgressBar.visibility = View.VISIBLE
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            // A service was found! Do something with it.
            Log.d(TAG, "Service discovery success: $service")

            when {
                service.serviceType != SERVICE_TYPE -> {
                    Log.d(TAG, "Unknown Service Type: ${service.serviceType}")
                }
                service.serviceName.contains(SERVICE_NAME) -> {
                    nsdManager.resolveService(service, ResolveListener())
                }
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            Log.e(TAG, "service lost: $service")
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i(TAG, "Discovery stopped: $serviceType")
            rootView.nsdScanProgressBar.visibility = View.INVISIBLE
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            rootView.nsdScanProgressBar.visibility = View.INVISIBLE
            Log.e(TAG, "onStartDiscoveryFailed: $errorCode")

            showError("DNS-SD error: $errorCode\nTry manual connect")
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "onStopDiscoveryFailed: $errorCode")

            showError("DNS-SD error: $errorCode\nTry manual connect")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.login_fragment, container, false).also {
            rootView = it

            nsdManager = (context?.getSystemService(Context.NSD_SERVICE) as NsdManager)
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)

            serverListAdapter = ServerListAdapter()

            viewModel.cachedAddress.observe(viewLifecycleOwner, Observer { address ->
                addressInputEditText.setText(address)
            })

            viewModel.addressSaveStatus.observe(viewLifecycleOwner, Observer(this::handleSaveStatus))

            it.scannedServerList.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this.context)
                adapter = serverListAdapter

                addItemDecoration(
                    DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
                )
            }

            it.connectButton.setOnClickListener {
                val address = addressInputEditText.text.toString()
                viewModel.cacheAddress(address)
                viewModel.saveAddress(address)
            }

            serverListAdapter.setOnItemClickListener { server ->
                val address = "${server.host.hostAddress}:${server.port}"
                viewModel.cacheAddress(address)
                viewModel.saveAddress(address)
            }

            requireActivity().onBackPressedDispatcher.addCallback(this) {
                requireActivity().finish()
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

    private fun showError(error: String) {
        rootView.errorText.text = error
        rootView.errorText.visibility = View.VISIBLE
        rootView.nsdScanProgressBar.visibility = View.GONE
        rootView.scannedServerList.visibility = View.GONE
    }

    inner class ResolveListener : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Called when the resolve fails. Use the error code to debug.
            Log.e(TAG, "Resolve failed: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.i(TAG, "Resolve Succeeded: $serviceInfo")
            servers.add(Server(serviceInfo.serviceName, serviceInfo.host, serviceInfo.port))

            this@LoginFragment.lifecycleScope.launch {
                serverListAdapter.updateList(servers)
            }
        }
    }
}
