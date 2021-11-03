package ru.solom.magiclamp.main

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.solom.magiclamp.data.EffectDto
import ru.solom.magiclamp.data.LampState
import ru.solom.magiclamp.data.SpRepository
import ru.solom.magiclamp.di.MutableAddress
import ru.solom.magiclamp.lampValues
import javax.inject.Inject

@Suppress("TooManyFunctions")
class MainInteractor @Inject constructor(
    private val spRepository: SpRepository,
    private val repository: MainRepository,
    @Suppress("ConstructorParameterNaming")
    @MutableAddress
    private val _addressFlow: MutableStateFlow<String?>
) {
    private val _addressResultFlow = MutableStateFlow<Result<String>?>(null).apply {
        onEach {
            _addressFlow.value = it?.getOrNull()
        }.launchIn(CoroutineScope(Dispatchers.IO))
    }
    val addressFlow = _addressResultFlow.asStateFlow()

    private val _lampState = MutableStateFlow(LampState())
    val lampState: StateFlow<LampState> = _lampState.asStateFlow()

    private val _effects = MutableStateFlow(emptyList<EffectDto>())
    val effects = _effects.asStateFlow()

    suspend fun getInitialAddress() {
        val spAddr = spRepository.getAddress()?.takeIf {
            (repository.tryConnect(it)?.lampValues?.size ?: 0) > 2
        }
        val addr = spAddr ?: repository.discoverLamp()?.lampValues?.getAddress()
            ?.also { spRepository.storeAddress(it) }
        _addressResultFlow.value = if (addr == null) {
            Result.failure(IllegalArgumentException("Address not discovered"))
        } else {
            Result.success(addr)
        }
    }

    suspend fun initEffects() {
        val cachedEffects = spRepository.getEffectsSet()?.map { EffectDto.fromString(it) }
        setEffects(cachedEffects ?: getEffectsFromLamp())
    }

    suspend fun sendPowerSwitch(isTurnOn: Boolean) {
        repository.sendPowerChange(isTurnOn)
            .apply { _lampState.value = LampState.fromValues(this?.lampValues) }
    }

    suspend fun getCurrentState() {
        val r = repository.getCurrentState()
        _lampState.value = r?.lampValues?.toLampState() ?: LampState()
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

    suspend fun invalidateEffects() {
        setEffects(getEffectsFromLamp())
    }

    private suspend fun getEffectsFromLamp(): List<EffectDto> {
        val effects = repository.getEffectsList().map { EffectDto.fromString(it) }.toList()
        spRepository.storeEffectsSet(effects.map { it.toString() })
        return effects
    }

    suspend fun setEffect(id: Int) {
        _lampState.value = repository.setCurrentEffect(id)?.lampValues?.toLampState() ?: LampState()
    }

    private suspend fun setEffects(effects: List<EffectDto>) {
        _effects.value = effects
        getCurrentState()
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
