package ru.solom.magiclamp

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(private val activityProvider: ActivityProvider) {
    private val sp: SharedPreferences
        get() = activityProvider.requireActivity()
            .getSharedPreferences(SP_MAIN, Context.MODE_PRIVATE)

    private val socket: DatagramSocket
        get() = DatagramSocket(null).apply {
            reuseAddress = true
            soTimeout = 10000
        }
    private var currentAddress: String? = null

    fun storeAddress(addr: String) {
        currentAddress = addr
        sp.edit().putString(KEY_ADDR, addr).apply()
    }

    fun getAddress(): String? {
        currentAddress = sp.getString(KEY_ADDR, null)
        return currentAddress
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun discoverLamp() = withSocket {
        val response = DatagramPacket(ByteArray(BUF_SIZE), BUF_SIZE)
        forAllIpAddresses { i, j -> send(DISCOVER_COMMAND.asPacket("192.168.${i}.${j}")) }
        receive(response)
        return@withSocket String(response.data)
    }

    suspend fun sendPowerChange(isTurnOn: Boolean) = withSocket {
        val command = if (isTurnOn) P_ON_COMMAND else P_OFF_COMMAND
        val resp = sendWithResult(command)
        return@withSocket resp?.let { String(it) }
    }

    suspend fun sendBrightnessChange(value: Int) = withSocket {
        sendWithResult(BRIGHTNESS_COMMAND + value.toString())
    }

    suspend fun getCurrentState() = withSocket {
        return@withSocket sendWithResult(GET_COMMAND).asString()
    }

    suspend fun getEffectsList(): Sequence<String> {
        var result = sequenceOf<String>()
        for (i in 1..3) {
            val chunk = withSocket {
                sendWithResult("LIST $i")?.asString()?.substringBeforeLast("LIST")
            } ?: continue
            result += chunk.splitToSequence(';').drop(1).filter { it != "\n" }
        }
        return result
    }

    private inline fun forAllIpAddresses(block: (Int, Int) -> Unit) = repeat(255) { i ->
        repeat(255) { j -> block(i, j) }
    }

    private fun DatagramSocket.sendWithResult(data: String): ByteArray? {
        bind(InetSocketAddress(SOCKET_PORT))
        val response = DatagramPacket(ByteArray(BUF_SIZE), BUF_SIZE)
        Log.d("Sent!", data)
        send(data.asPacket())
        try {
            receive(response)
        } catch (e: SocketTimeoutException) {
            Log.e(this@MainRepository::class.java.canonicalName, e.stackTraceToString())
        }
        return response.data
    }

    private suspend inline fun <T> withSocket(
        crossinline block: DatagramSocket.() -> T
    ) = withContext(Dispatchers.IO) {
        val tmp = socket.use(block)
        Log.d("Received!", tmp.toString())
        return@withContext tmp
    }

    private fun String.asPacket(addr: String? = currentAddress, port: Int = 8888): DatagramPacket {
        val data = toByteArray()
        return DatagramPacket(data, data.size, Inet4Address.getByName(addr), port)
    }

    private fun ByteArray?.asString() = this?.let { String(it) }
}

private const val KEY_ADDR = "addr"
private const val DISCOVER_COMMAND = "DISCOVER"
private const val P_ON_COMMAND = "P_ON"
private const val P_OFF_COMMAND = "P_OFF"
private const val BRIGHTNESS_COMMAND = "BRI"
private const val GET_COMMAND = "GET"
private const val BUF_SIZE = 1000
private const val SOCKET_PORT = 5000
