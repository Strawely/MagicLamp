package ru.solom.magiclamp.main

import kotlinx.coroutines.sync.Mutex
import ru.solom.magiclamp.BRIGHTNESS_COMMAND
import ru.solom.magiclamp.DISCOVER_COMMAND
import ru.solom.magiclamp.EFFECTS_LIST_COMMAND
import ru.solom.magiclamp.EFFECT_COMMAND
import ru.solom.magiclamp.GET_COMMAND
import ru.solom.magiclamp.LampRepository
import ru.solom.magiclamp.LampRepositoryFactory
import ru.solom.magiclamp.P_OFF_COMMAND
import ru.solom.magiclamp.P_ON_COMMAND
import ru.solom.magiclamp.SCALE_COMMAND
import ru.solom.magiclamp.SPEED_COMMAND
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val lampRepositoryFactory: LampRepositoryFactory,
    private val mutex: Mutex
) {
    private suspend fun getLampRepo() = lampRepositoryFactory.getLampRepository()

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun discoverLamp() =
        LampRepository("", mutex).sendBroadcastWithResult(DISCOVER_COMMAND)
    suspend fun tryConnect(address: String) =
        LampRepository(address, mutex).sendWithResult(GET_COMMAND)

    suspend fun sendPowerChange(isTurnOn: Boolean) =
        getLampRepo().sendWithResult(if (isTurnOn) P_ON_COMMAND else P_OFF_COMMAND)

    suspend fun sendBrightnessChange(value: Int) =
        getLampRepo().send(BRIGHTNESS_COMMAND, value)

    suspend fun sendSpeedChange(value: Int) = getLampRepo().send(SPEED_COMMAND, value)
    suspend fun sendScaleChange(value: Int) = getLampRepo().send(SCALE_COMMAND, value)
    suspend fun getCurrentState(): String? {
        println("getCurrentState")
        return getLampRepo().sendWithResult(GET_COMMAND)
    }

    suspend fun getEffectsList(): Sequence<String> {
        var result = sequenceOf<String>()
        for (i in 1..EFFECTS_PAGES_NUMBER) {
            result += getLampRepo().sendWithResult(EFFECTS_LIST_COMMAND, i)
                ?.splitToSequence(';')
                ?.drop(1)
                ?.filter { !it.startsWith("\n") }
                ?: continue
        }
        return result
    }

    suspend fun setCurrentEffect(id: Int) =
        getLampRepo().sendWithResult(EFFECT_COMMAND + id.toString())
}

private const val EFFECTS_PAGES_NUMBER = 3
