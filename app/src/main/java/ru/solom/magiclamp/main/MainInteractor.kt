package ru.solom.magiclamp.main

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.solom.magiclamp.data.EffectDto
import ru.solom.magiclamp.data.LampState
import ru.solom.magiclamp.data.SpRepository
import ru.solom.magiclamp.lampValues
import javax.inject.Inject

@Suppress("TooManyFunctions")
class MainInteractor @Inject constructor(private val spRepository: SpRepository) {
    private val _addressFlow = MutableStateFlow<String?>(null)
    val addressFlow = _addressFlow.asStateFlow()

    private val _lampState = MutableStateFlow(LampState())
    val lampState: StateFlow<LampState> = _lampState.asStateFlow()

    private val _effects = MutableStateFlow(emptyList<EffectDto>())
    val effects = _effects.asStateFlow()

    private val repository = MainRepository(addressFlow)

    suspend fun getInitialAddress() {
        _addressFlow.value = spRepository.getAddress()
        if (_addressFlow.value == null) {
            val discoveredAddress = repository.discoverLamp()?.lampValues?.getAddress() ?: return
            spRepository.storeAddress(discoveredAddress)
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

    suspend fun updateEffects() {
        _effects.value = repository.getEffectsList().map { EffectDto.fromString(it) }.toList()
    }

    suspend fun setEffect(id: Int) {
        _lampState.value = repository.setCurrentEffect(id)?.lampValues?.toLampState() ?: LampState()
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

private const val STATE_REFRESH_DELAY = 500L
private const val PARAM_MAX_VALUE = 255
