package ua.senko.remoteinput.data

import java.net.InetAddress

data class Server(
    val name: String,
    val host: InetAddress,
    val port: Int
)
