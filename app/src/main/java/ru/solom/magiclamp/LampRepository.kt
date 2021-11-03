package ru.solom.magiclamp

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.SocketTimeoutException

@Suppress("FunctionNaming")
fun LampRepository(address: String?, mutex: Mutex) = address?.let { LampRepository(it, mutex) }

class LampRepository constructor(private val address: String, private val mutex: Mutex) {
    private val socket: DatagramSocket
        get() = DatagramSocket(null).apply {
            reuseAddress = true
            soTimeout = SOCKET_TIMEOUT
        }

    suspend fun sendBroadcastWithResult(data: String): String? = withSocket {
        val response = DatagramPacket(ByteArray(BUF_SIZE), BUF_SIZE)
        forLocalIpAddresses { i, j -> send(data.asPacket("192.168.$i.$j")) }
        try {
            receive(response)
        } catch (e: SocketTimeoutException) {
            Log.e(this::class.java.canonicalName, e.stackTraceToString())
        }
        return@withSocket response.data.asString()
    }

    suspend fun sendWithResult(data: String, vararg params: Any): String? = withSocket {
        bind(InetSocketAddress(SOCKET_PORT))
        val dataToSend = data + params.joinToString("", transform = { it.toString() })
        Log.d("Sent!", dataToSend)
        val response = DatagramPacket(ByteArray(BUF_SIZE), BUF_SIZE)
        send(dataToSend.asPacket())
        try {
            receive(response)
        } catch (e: SocketTimeoutException) {
            Log.e(this::class.java.canonicalName, e.stackTraceToString())
        }
        response.data?.asString()
    }

    suspend fun send(data: String, vararg params: Any) = withSocket {
        bind(InetSocketAddress(SOCKET_PORT))
        val dataToSend = data + params.joinToString("", transform = { it.toString() })
        Log.d("Sent!", dataToSend)
        send(dataToSend.asPacket())
    }

    private suspend inline fun <T> withSocket(
        crossinline block: suspend DatagramSocket.() -> T
    ) = withContext(Dispatchers.IO) {
        mutex.withLock {
            async {
                val tmp = socket.use { block(it) }
                Log.d("Received!", tmp.toString())
                return@async tmp
            }.await()
        }
    }

    private fun ByteArray?.asString(): String? {
        val valuableItems = this?.filter { it != 0.toByte() }
        return if (valuableItems.isNullOrEmpty()) {
            null
        } else {
            String(valuableItems.toByteArray())
        }
    }

    private fun String.asPacket(addr: String? = address, port: Int = 8888): DatagramPacket {
        val data = toByteArray()
        return DatagramPacket(data, data.size, Inet4Address.getByName(addr), port)
    }

    private inline fun forLocalIpAddresses(block: (Int, Int) -> Unit) =
        repeat(MAX_IP_SEGMENT) { i ->
            repeat(MAX_IP_SEGMENT) { j -> block(i, j) }
        }
}

private const val SOCKET_PORT = 5000
private const val SOCKET_TIMEOUT = 15_000
private const val MAX_IP_SEGMENT = 255
const val BUF_SIZE = 1024
