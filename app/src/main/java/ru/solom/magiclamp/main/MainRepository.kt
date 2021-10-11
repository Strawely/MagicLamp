package ru.solom.magiclamp.main

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import ru.solom.magiclamp.BRIGHTNESS_COMMAND
import ru.solom.magiclamp.DISCOVER_COMMAND
import ru.solom.magiclamp.EFFECTS_LIST_COMMAND
import ru.solom.magiclamp.EFFECT_COMMAND
import ru.solom.magiclamp.GET_COMMAND
import ru.solom.magiclamp.P_OFF_COMMAND
import ru.solom.magiclamp.P_ON_COMMAND
import ru.solom.magiclamp.SCALE_COMMAND
import ru.solom.magiclamp.SPEED_COMMAND
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.SocketTimeoutException

@Suppress("TooManyFunctions")
class MainRepository constructor(private val addressFlow: StateFlow<String?>) {
    private val socket: DatagramSocket
        get() = DatagramSocket(null).apply {
            reuseAddress = true
            soTimeout = SOCKET_TIMEOUT
        }

    private val currentAddress get() = addressFlow.value

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun discoverLamp() = withSocket {
        val response = DatagramPacket(ByteArray(BUF_SIZE), BUF_SIZE)
        forAllIpAddresses { i, j -> send(DISCOVER_COMMAND.asPacket("192.168.$i.$j")) }
        receive(response)
        return@withSocket response.data.asString()
    }

    suspend fun sendPowerChange(isTurnOn: Boolean) = withSocket {
        sendWithResult(if (isTurnOn) P_ON_COMMAND else P_OFF_COMMAND).asString()
    }

    suspend fun sendBrightnessChange(value: Int) = withSocket { send(BRIGHTNESS_COMMAND, value) }
    suspend fun sendSpeedChange(value: Int) = withSocket { send(SPEED_COMMAND, value) }
    suspend fun sendScaleChange(value: Int) = withSocket { send(SCALE_COMMAND, value) }
    suspend fun getCurrentState() = withSocket { sendWithResult(GET_COMMAND).asString() }

    suspend fun getEffectsList(): Sequence<String> {
        var result = sequenceOf<String>()
        for (i in 1..EFFECTS_PAGES_NUMBER) {
            result += withSocket {
                sendWithResult(EFFECTS_LIST_COMMAND, i)?.asString()
                    ?.splitToSequence(';')
                    ?.drop(1)
                    ?.filter { !it.startsWith("\n") }
            } ?: continue
        }
        return result
    }

    suspend fun setCurrentEffect(id: Int) = withSocket {
        sendWithResult(EFFECT_COMMAND + id.toString()).asString()
    }

    private inline fun forAllIpAddresses(block: (Int, Int) -> Unit) = repeat(MAX_IP_SEGMENT) { i ->
        repeat(MAX_IP_SEGMENT) { j -> block(i, j) }
    }

    private fun DatagramSocket.sendWithResult(data: String, vararg params: Any): ByteArray? {
        val response = DatagramPacket(ByteArray(BUF_SIZE), BUF_SIZE)
        send(data, *params)
        try {
            receive(response)
        } catch (e: SocketTimeoutException) {
            Log.e(this@MainRepository::class.java.canonicalName, e.stackTraceToString())
        }
        return response.data
    }

    private fun DatagramSocket.send(data: String, vararg params: Any) {
        bind(InetSocketAddress(SOCKET_PORT))
        val dataToSend = data + params.joinToString("", transform = { it.toString() })
        Log.d("Sent!", dataToSend)
        send(dataToSend.asPacket())
    }

    private suspend inline fun <T> withSocket(
        crossinline block: DatagramSocket.() -> T
    ) = withContext(Dispatchers.IO) {
        async {
            val tmp = socket.use(block)
            Log.d("Received!", tmp.toString())
            return@async tmp
        }.await()
    }
    private fun ByteArray?.asString() = this?.let { String(it) }

    private fun String.asPacket(addr: String? = currentAddress, port: Int = 8888): DatagramPacket {
        val data = toByteArray()
        return DatagramPacket(data, data.size, Inet4Address.getByName(addr), port)
    }
}

private const val BUF_SIZE = 1024
private const val SOCKET_PORT = 5000
private const val SOCKET_TIMEOUT = 10_000
private const val MAX_IP_SEGMENT = 255
private const val EFFECTS_PAGES_NUMBER = 3
