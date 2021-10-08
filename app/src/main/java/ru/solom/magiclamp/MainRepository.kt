package ru.solom.magiclamp

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("TooManyFunctions")
@Singleton
class MainRepository @Inject constructor(private val activityProvider: ActivityProvider) {
    private val sp: SharedPreferences
        get() = activityProvider.requireActivity()
            .getSharedPreferences(SP_MAIN, Context.MODE_PRIVATE)

    private val socket: DatagramSocket
        get() = DatagramSocket(null).apply {
            reuseAddress = true
            soTimeout = SOCKET_TIMEOUT
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
        send(data, params)
        try {
            receive(response)
        } catch (e: SocketTimeoutException) {
            Log.e(this@MainRepository::class.java.canonicalName, e.stackTraceToString())
        }
        return response.data
    }

    private fun DatagramSocket.send(data: String, vararg params: Any) {
        bind(InetSocketAddress(SOCKET_PORT))
        Log.d("Sent!", data)
        send((data + params.joinToString("", transform = { it.toString() })).asPacket())
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
private const val SPEED_COMMAND = "SPD"
private const val SCALE_COMMAND = "SCA"
private const val GET_COMMAND = "GET"
private const val EFFECT_COMMAND = "EFF"
private const val EFFECTS_LIST_COMMAND = "LIST"
private const val BUF_SIZE = 1000
private const val SOCKET_PORT = 5000
private const val SOCKET_TIMEOUT = 10_000
private const val MAX_IP_SEGMENT = 255
private const val EFFECTS_PAGES_NUMBER = 3
