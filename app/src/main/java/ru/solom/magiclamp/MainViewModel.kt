package ru.solom.magiclamp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val interactor: MainInteractor) : ViewModel() {

    private val _mainState = MutableStateFlow(MainState())
    val mainState = _mainState.asStateFlow()

    private val _effects = MutableStateFlow(emptyList<EffectDto>())
    val effects = _effects.asStateFlow()

    private var brightnessJob: Job? = null

    init {
        viewModelScope.launch {
            interactor.addressFlow.collect {
                _mainState.value = _mainState.value.copy(address = it)
            }
        }
        viewModelScope.launch {
            interactor.getInitialAddress()
            interactor.getCurrentState()
            interactor.getEffects().let { newEffects ->
                _effects.value = newEffects
            }
        }
        viewModelScope.launch {
            interactor.lampState.collect {
                _mainState.value = _mainState.value.copy(lampState = it)
            }
        }
    }

    fun onPowerBtnClick() = viewModelScope.launch {
        interactor.sendPowerSwitch(!_mainState.value.lampState.isOn)
    }

    fun onBrightnessChanged(value: Int) {
        brightnessJob?.cancel()
        brightnessJob = viewModelScope.launch {
            delay(DEBOUNCE_DELAY)
            interactor.setBrightness(value)
        }
    }
}

data class MainState(
    val address: String? = null,
    val progress: Boolean = false,
    val lampState: LampState = LampState(),
)

private const val DEBOUNCE_DELAY = 100L
