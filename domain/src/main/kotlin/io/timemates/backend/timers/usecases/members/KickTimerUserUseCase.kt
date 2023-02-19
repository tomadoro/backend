package io.timemates.backend.timers.usecases.members

import io.timemates.backend.features.authorization.AuthorizedContext
import io.timemates.backend.timers.repositories.TimersRepository
import io.timemates.backend.timers.types.TimerAuthScope
import io.timemates.backend.timers.types.value.TimerId
import io.timemates.backend.users.types.value.UserId
import io.timemates.backend.users.types.value.userId

class KickTimerUserUseCase(
    private val timersRepository: TimersRepository,
) {
    context(AuthorizedContext<TimerAuthScope.Write>)
    suspend fun execute(
        timerId: TimerId,
        userToKick: UserId,
    ): Result {
        if (timersRepository.getTimerInformation(timerId)?.ownerId != userId)
            return Result.NoAccess

        timersRepository.removeMember(userToKick, timerId)
        return Result.Success
    }

    sealed interface Result {
        object Success : Result
        object NoAccess : Result
    }
}