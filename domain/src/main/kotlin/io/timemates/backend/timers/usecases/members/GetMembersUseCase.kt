package io.timemates.backend.timers.usecases.members

import io.timemates.backend.features.authorization.AuthorizedContext
import io.timemates.backend.timers.repositories.TimersRepository
import io.timemates.backend.timers.types.TimerAuthScope
import io.timemates.backend.common.types.value.Count
import io.timemates.backend.timers.types.value.TimerId
import io.timemates.backend.users.repositories.UsersRepository
import io.timemates.backend.users.types.User
import io.timemates.backend.users.types.value.UserId
import io.timemates.backend.users.types.value.userId

class GetMembersUseCase(
    private val timersRepository: TimersRepository,
    private val usersRepository: UsersRepository,
) {
    context(AuthorizedContext<TimerAuthScope.Read>)
    suspend fun execute(
        timerId: TimerId,
        lastId: UserId?,
        count: Count,
    ): Result {
        if (!timersRepository.isMemberOf(userId, timerId))
            return Result.NoAccess

        val members = timersRepository.getMembers(
            timerId, lastId, count
        )

        val membersFullInfo = usersRepository.getUsers(members)

        return Result.Success(membersFullInfo, members.last())
    }

    sealed interface Result {
        class Success(
            val list: List<User>,
            val lastId: UserId,
        ) : Result

        data object NoAccess : Result
    }
}