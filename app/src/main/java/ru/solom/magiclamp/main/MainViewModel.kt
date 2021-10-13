package ru.solom.magiclamp.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.solom.magiclamp.data.LampState
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val interactor: MainInteractor) : ViewModel() {

    private val _mainState = MutableStateFlow(MainState())
    val mainState = _mainState.asStateFlow()

    val effects = interactor.effects

    init {
        interactor.addressFlow.onEach {
            _mainState.value = _mainState.value.copy(address = it)
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            interactor.getInitialAddress()
            interactor.getCurrentState()
            interactor.updateEffects()
        }

        interactor.lampState.onEach {
            _mainState.value = _mainState.value.copy(lampState = it)
        }.launchIn(viewModelScope)
    }

    fun onPowerBtnClick() = viewModelScope.launch {
        interactor.sendPowerSwitch(!_mainState.value.lampState.isOn)
    }

    fun onBrightnessChanged(value: Int) = viewModelScope.launch { interactor.setBrightness(value) }
    fun onSpeedChanged(value: Int) = viewModelScope.launch { interactor.setSpeed(value) }
    fun onScaleChanged(value: Int) = viewModelScope.launch { interactor.setScale(value) }
    fun onEffectsRefresh() = viewModelScope.launch {
        _mainState.value = _mainState.value.copy(isEffectsRefreshing = true)
        interactor.invalidateEffects()
        _mainState.value = _mainState.value.copy(isEffectsRefreshing = false)
    }

    fun onItemClick(id: Int) = viewModelScope.launch {
        interactor.setEffect(id)
    }
}

data class MainState(
    val address: String? = null,
    val progress: Boolean = false,
    val lampState: LampState = LampState(),
    val isEffectsRefreshing: Boolean = false
)
