package ru.solom.magiclamp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("TooManyFunctions")
class MainInteractor @Inject constructor(private val repository: MainRepository) {
    private val _addressFlow = MutableStateFlow<String?>(null)
    val addressFlow = _addressFlow.asStateFlow()

    private val _lampState = MutableStateFlow(LampState(isOn = false))
    val lampState = _lampState.asStateFlow()

    suspend fun getInitialAddress() {
        _addressFlow.value = repository.getAddress()
        if (_addressFlow.value == null) {
            val discoveredAddress = repository.discoverLamp()?.lampValues?.getAddress() ?: return
            repository.storeAddress(discoveredAddress)
            _addressFlow.value = discoveredAddress
        }
    }

    suspend fun sendPowerSwitch(isTurnOn: Boolean) {
        _addressFlow.value ?: return
        repository.sendPowerChange(isTurnOn)
            .apply { _lampState.value = LampState.fromValues(this?.lampValues) }
    }

    suspend fun getCurrentState() {
        _lampState.value = repository.getCurrentState()?.lampValues?.toLampState() ?: LampState()
    }

    suspend fun setBrightness(value: Int) {
        val constrainedValue = value.coerceIn(0, PARAM_MAX_VALUE)
        repository.sendBrightnessChange(constrainedValue)
        refreshState()
    }

    suspend fun setSpeed(value: Int) {
        val constrainedValue = value.coerceIn(0, PARAM_MAX_VALUE)
        repository.sendSpeedChange(constrainedValue)
        refreshState()
    }

    suspend fun setScale(value: Int) {
        val constrainedValue = value.coerceIn(0, PARAM_MAX_VALUE)
        repository.sendScaleChange(constrainedValue)
        refreshState()
    }

    suspend fun getEffects(): List<EffectDto> {
        val effects = repository.getEffectsList()
        val result = effects.map { EffectDto.fromString(it) }.toList()
        return result
    }

    suspend fun setEffect(id: Int) {
        repository.setCurrentEffect(id)?.lampValues?.toLampState()?.let { _lampState.value = it}
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

@Suppress("MagicNumber")
data class LampState(
    val currentId: Int = 0,
    val brightness: Int = 0,
    val speed: Int = 0,
    val scale: Int = 0,
    val isOn: Boolean = false,
) {
    companion object {
        fun fromValues(values: List<String>?) = if (values == null) {
            LampState()
        } else {
            LampState(
                currentId = values[1].toInt(),
                brightness = values[2].toInt(),
                speed = values[3].toInt(),
                scale = values[4].toInt(),
                isOn = values[5] == "1",
            )
        }
    }
}

private const val STATE_REFRESH_DELAY = 500L
private const val PARAM_MAX_VALUE = 255
