package ru.solom.magiclamp.alarms

import ru.solom.magiclamp.GET_ALARMS_COMMAND
import ru.solom.magiclamp.LampRepositoryFactory
import ru.solom.magiclamp.SET_ALARM_COMMAND
import ru.solom.magiclamp.lampValues
import javax.inject.Inject

class AlarmsRepository @Inject constructor(
    private val lampRepositoryFactory: LampRepositoryFactory
) {
    private suspend fun getLampRepo() = lampRepositoryFactory.getLampRepository()

    // response format X X X X X X X Y Y Y Y Y Y Y Z
    // where X - is alarm enabled on exact week day, Y - time on exact day, Z - offset from end
    suspend fun getAlarmsList() = send(GET_ALARMS_COMMAND)
    suspend fun setAlarmTime(weekDay: Int, time: Int) = send(SET_ALARM_COMMAND, weekDay, time)

    suspend fun setAlarmEnabled(weekDay: Int, isEnabled: Boolean): AlarmsDto? {
        val parameter = if (isEnabled) ALARM_ON else ALARM_OFF
        return send(SET_ALARM_COMMAND, weekDay, parameter)
    }

    private suspend fun send(command: String, vararg params: Any): AlarmsDto? {
        return getLampRepo().sendWithResult(command, *params)
            ?.lampValues
            ?.drop(1)
            ?.chunked(WEEK_DAYS_COUNT)
            ?.let { values ->
                AlarmsDto(
                    enabledStates = values[0].map { it.isTrue() },
                    timeValues = values[1].map { it.toInt() },
                    offset = values[2].first().toInt()
                )
            }
    }

    private fun String.isTrue() = toInt() > 0
}

data class AlarmsDto(
    val enabledStates: List<Boolean>,
    val timeValues: List<Int>,
    val offset: Int
)

private const val ALARM_ON = "_ON"
private const val ALARM_OFF = "_OFF"
const val WEEK_DAYS_COUNT = 7
