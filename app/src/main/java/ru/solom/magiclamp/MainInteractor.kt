package ru.solom.magiclamp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.net.DatagramSocket
import javax.inject.Inject

class MainInteractor @Inject constructor(private val repository: MainRepository) {
    private val _addressFlow = MutableStateFlow<String?>(null)
    val addressFlow = _addressFlow.asStateFlow()

    private val _lampState = MutableStateFlow(LampState(isOn = false))
    val lampState = _lampState.asStateFlow()

    suspend fun getInitialAddress() {
        _addressFlow.value = repository.getAddress()
        if (_addressFlow.value == null) {
            val discoveredAddress = repository.discoverLamp().getAddress()
            repository.storeAddress(discoveredAddress)
            _addressFlow.value = discoveredAddress
        }
    }

    suspend fun sendPowerSwitch(isTurnOn: Boolean) = withContext(Dispatchers.IO) {
        _addressFlow.value ?: return@withContext
        repository.sendPowerChange(isTurnOn).apply { _lampState.value = LampState.fromValues(this) }
    }

    private fun List<String>.getAddress() = get(1).substringBefore(':')
}

data class LampState(
    val isOn: Boolean,
) {
    companion object {
        fun fromValues(values: List<String>) = LampState(
            isOn = values[5] == "1"
        )
    }
}
