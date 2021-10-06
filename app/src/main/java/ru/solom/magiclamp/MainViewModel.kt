package ru.solom.magiclamp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val interactor: MainInteractor) : ViewModel() {

    private val _mainState = MutableStateFlow(MainState("", progress = false))
    val mainState = _mainState.asStateFlow()

    init {
        viewModelScope.launch {
            interactor.addressFlow.collect {
                _mainState.value = _mainState.value.copy(address = it)
            }
        }
        viewModelScope.launch {
            interactor.getInitialAddress()
        }
        viewModelScope.launch {
            interactor.lampState.collect {

            }
        }
    }

    fun onPowerSwitchCheck(isChecked: Boolean) = viewModelScope.launch {
        interactor.sendPowerSwitch(isChecked)
    }
}

data class MainState(
    val address: String?,
    val progress: Boolean
)
