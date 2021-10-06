package ru.solom.magiclamp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
            val discoveredAddress = repository.discoverLamp().lampValues.getAddress()
            repository.storeAddress(discoveredAddress)
            _addressFlow.value = discoveredAddress
        }
        getEffects()
    }

    suspend fun sendPowerSwitch(isTurnOn: Boolean) {
        _addressFlow.value ?: return
        repository.sendPowerChange(isTurnOn)
            .apply { _lampState.value = LampState.fromValues(this?.lampValues) }
    }

    suspend fun getCurrentState() {
        val result = repository.getCurrentState() ?: return
        _lampState.value = LampState.fromValues(result.lampValues)
    }

    suspend fun setBrightness(value: Int) {
        val constrainedValue = value.coerceIn(0, 255)
        repository.sendBrightnessChange(constrainedValue)
        refreshState()
    }

    suspend fun getEffects() {
        val effects = repository.getEffectsList() ?: return
        println(effects.drop(1).joinToString("\n"))
    }

    private var refreshJob: Job? = null
    private suspend fun refreshState() {
        refreshJob?.cancel()
        refreshJob = CoroutineScope(Dispatchers.IO).launch {
            delay(STATE_REFRESH_DELAY)
            val currState = repository.getCurrentState() ?: return@launch
            _lampState.value = currState.lampValues.toLampState()
        }
    }

    private fun List<String>.getAddress() = get(1).substringBefore(':')

    private fun List<String>.toLampState() = LampState.fromValues(this)
}

data class LampState(
    val isOn: Boolean = false,
    val brightness: Int = 0
) {
    companion object {
        fun fromValues(values: List<String>?) = if (values == null) {
            LampState()
        } else {
            LampState(
                isOn = values[5] == "1",
                brightness = values[2].toInt()
            )
        }
    }
}

private const val STATE_REFRESH_DELAY = 500L
