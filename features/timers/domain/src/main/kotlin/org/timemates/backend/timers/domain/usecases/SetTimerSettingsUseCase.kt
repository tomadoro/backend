package org.timemates.backend.timers.domain.usecases

import org.timemates.backend.core.types.integration.auth.userId
import org.timemates.backend.foundation.authorization.Authorized
import org.timemates.backend.timers.domain.repositories.TimersRepository
import org.timemates.backend.types.timers.TimerSettings
import org.timemates.backend.types.timers.TimersScope
import org.timemates.backend.types.timers.value.TimerId

class SetTimerSettingsUseCase(
    private val timers: TimersRepository,
) {

    suspend fun execute(
        auth: Authorized<TimersScope.Write>,
        timerId: TimerId,
        newSettings: TimerSettings.Patch,
    ): Result {
        if (timers.getTimerInformation(timerId)?.ownerId != auth.userId)
            return Result.NoAccess

        timers.setTimerSettings(
            timerId,
            newSettings
        )

        return Result.Success
    }

    sealed interface Result {
        data object Success : Result
        data object NoAccess : Result
    }
}