package ua.senko.remoteinput.ui.login

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.server_list_item.view.*
import ua.senko.remoteinput.R
import ua.senko.remoteinput.data.Server

class ServerListAdapter : RecyclerView.Adapter<ServerListAdapter.ViewHolder>() {
    private val servers: MutableList<Server> = mutableListOf()

    private var itemClickListener: (Server) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.server_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = servers[position]
        holder.index = position
        holder.name.text = item.name
        holder.address.text = "${item.host.hostAddress}:${item.port}"
    }

    override fun getItemCount(): Int = servers.size

    fun updateList(newServers: List<Server>) {
        servers.clear()
        servers.addAll(newServers)
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(callback: (Server) -> Unit) {
        itemClickListener = callback
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.serverNameText
        val address: TextView = view.serverAddressText
        var index: Int? = null

        init {
            view.setOnClickListener { _ ->
                index?.let { itemClickListener(servers[it]) }
            }
        }
    }
}