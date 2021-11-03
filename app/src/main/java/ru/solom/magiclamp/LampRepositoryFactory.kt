package ru.solom.magiclamp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import ru.solom.magiclamp.di.Address
import ru.solom.magiclamp.di.LampJob
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LampRepositoryFactory @Inject constructor(@Address addressFlow: StateFlow<String?>, mutex: Mutex) {
    private var _lampRepository: LampRepository? = null

    init {
        addressFlow.onEach { _lampRepository = LampRepository(it, mutex) }
            .launchIn(CoroutineScope(Dispatchers.IO ))
    }

    suspend fun getLampRepository(): LampRepository = withContext(Dispatchers.IO ) {
        suspendCoroutine { cont ->
            while (true) {
                val repo = _lampRepository
                if (repo != null) {
                    cont.resume(repo)
                    break
                }
            }
        }
    }
}
