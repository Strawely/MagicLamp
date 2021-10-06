package ru.solom.magiclamp

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(private val activityProvider: ActivityProvider) {
    private val sp: SharedPreferences
        get() = activityProvider.requireActivity()
            .getSharedPreferences(SP_MAIN, Context.MODE_PRIVATE)

    private var prevSocket: DatagramSocket? = null
    private val socket: DatagramSocket = DatagramSocket(5000)
    private val responsePacket = DatagramPacket(ByteArray(BUF_SIZE), BUF_SIZE)
    private var currentAddress: String = ""

    fun storeAddress(addr: String) {
        currentAddress = addr
        sp.edit().putString(KEY_ADDR, addr).apply()
    }

    fun getAddress(): String? {
        return sp.getString(KEY_ADDR, null)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun discoverLamp() = withSocket {
        forAllIpAddresses { i, j -> send(DISCOVER_COMMAND.asPacket("192.168.${i}.${j}")) }
        receive(responsePacket)
        return@withSocket responsePacket.data.lampValues
    }

    suspend fun sendPowerChange(isTurnOn: Boolean) = withSocket {
        if (isTurnOn) {
            send("LIST 1")
            receive(responsePacket)
            return@withSocket responsePacket.data.lampValues
        } else {
            val command = if (isTurnOn) P_ON_COMMAND else P_OFF_COMMAND
            send("GET")
            receive(responsePacket)
            return@withSocket responsePacket.data.lampValues
        }
    }

    private inline fun forAllIpAddresses(block: (Int, Int) -> Unit) {
        for (i in 0..255) {
            for (j in 0..255) {
                block(i, j)
            }
        }
    }

    private fun DatagramSocket.send(data: String) = send(data.asPacket())

    private suspend inline fun withSocket(
        crossinline block: DatagramSocket.() -> List<String>
    ): List<String> {
        return withContext(Dispatchers.IO) {
            val tmp = socket.use(block)
            Log.d("Received!", tmp.joinToString())
            return@withContext tmp
        }
    }

    private fun String.asPacket(addr: String = currentAddress, port: Int = 8888): DatagramPacket {
        val data = toByteArray()
        return DatagramPacket(data, data.size, Inet4Address.getByName(addr), port)
    }
}

private const val KEY_ADDR = "addr"
private const val DISCOVER_COMMAND = "DISCOVER"
private const val P_ON_COMMAND = "P_ON"
private const val P_OFF_COMMAND = "P_OFF"
private const val BUF_SIZE = 128
