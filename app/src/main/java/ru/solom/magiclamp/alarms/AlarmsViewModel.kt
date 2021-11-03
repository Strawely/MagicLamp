package ru.solom.magiclamp.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmsViewModel @Inject constructor(private val interactor: AlarmsInteractor) : ViewModel() {
    val alarmsState = interactor.alarms.map { res ->
        AlarmsList(
            data = res.getOrNull(),
            error = res.exceptionOrNull()?.message
        )
    }

    init {
        viewModelScope.launch { interactor.updateAlarmsList() }
    }

    fun onTimeClicked(state: AlarmState) {
        interactor.onTimeClicked(state.dayNum)
    }

    fun onEnabledSwitch(state: AlarmState, isChecked: Boolean) {
        viewModelScope.launch { interactor.setAlarmEnabled(state.dayNum, isChecked) }
    }
}

data class AlarmsList(
    val data: AlarmStateList? = null,
    val error: String? = null
)
