package ru.solom.magiclamp.alarms

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias AlarmStateList = List<AlarmState>

class AlarmsInteractor @Inject constructor(
    private val repository: AlarmsRepository,
    private val dialogsRouter: DialogsRouter
) {
    private val _alarms = MutableStateFlow<Result<AlarmStateList>>(Result.success(emptyList()))
    val alarms = _alarms.asStateFlow()

    suspend fun updateAlarmsList() {
        updateState(repository.getAlarmsList())
    }

    suspend fun setAlarmEnabled(weekDay: Int, isEnabled: Boolean) {
        val lampWeekDay = weekDay + 1
        updateState(repository.setAlarmEnabled(lampWeekDay, isEnabled))
    }

    fun onTimeClicked(weekDay: Int) {
        val currentData = _alarms.value.getOrNull() ?: return
        dialogsRouter.showTimePicker(currentData[weekDay].time) { pickedTime ->
            CoroutineScope(Dispatchers.IO).launch {
                onTimeChanged(weekDay, pickedTime)
            }
        }
    }

    private suspend fun onTimeChanged(weekDay: Int, time: Time) {
        val lampWeekDay = weekDay + 1
        updateState(repository.setAlarmTime(lampWeekDay, time.lampFormat))
    }

    private fun updateState(dto: AlarmsDto?) {
        if (dto == null) {
            _alarms.value = Result.failure(IllegalStateException("Network error"))
            return
        }
        val data = mutableListOf<AlarmState>().apply {
            repeat(WEEK_DAYS_COUNT) { i ->
                add(AlarmState(i, dto.enabledStates[i], Time.parseLampFormat(dto.timeValues[i])))
            }
        }
        _alarms.value = Result.success(data)
    }
}
