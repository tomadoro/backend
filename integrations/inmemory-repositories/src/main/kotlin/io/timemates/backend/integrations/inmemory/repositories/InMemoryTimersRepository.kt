package io.timemates.backend.integrations.inmemory.repositories

import io.timemates.backend.repositories.TimersRepository
import io.timemates.backend.repositories.UsersRepository
import io.timemates.backend.types.value.Count
import io.timemates.backend.types.value.UnixTime
import io.timemates.backend.types.value.TimerName

class InMemoryTimersRepository : TimersRepository {
    private data class Timer(
        var settings: TimersRepository.Settings,
        val ownerId: UsersRepository.UserId,
        val members: MutableList<UsersRepository.UserId>,
        val name: TimerName,
        val creationTime: UnixTime
    )

    private val timers: MutableList<Timer> = mutableListOf()

    override suspend fun createTimer(
        name: TimerName,
        settings: TimersRepository.Settings,
        ownerId: UsersRepository.UserId,
        creationTime: UnixTime
    ): TimersRepository.TimerId {
        timers.add(
            Timer(
                TimersRepository.Settings.Default,
                ownerId,
                mutableListOf(ownerId),
                name,
                creationTime
            )
        )
        return TimersRepository.TimerId(timers.lastIndex)
    }

    override suspend fun getTimer(timerId: TimersRepository.TimerId): TimersRepository.Timer? {
        return timers.getOrNull(timerId.int)?.let {
            TimersRepository.Timer(timerId, it.name, it.ownerId, it.settings)
        }
    }

    override suspend fun removeTimer(timerId: TimersRepository.TimerId) {
        timers.removeAt(timerId.int)
    }

    override suspend fun getOwnedTimersCount(ownerId: UsersRepository.UserId, after: UnixTime): Int {
        return timers.count { it.ownerId == ownerId && it.creationTime.long > after.long }
    }

    override suspend fun getTimerSettings(timerId: TimersRepository.TimerId): TimersRepository.Settings? {
        return timers.getOrNull(timerId.int)?.settings
    }

    override suspend fun setTimerSettings(timerId: TimersRepository.TimerId, settings: TimersRepository.NewSettings) {
        timers[timerId.int].settings = timers[timerId.int].settings.let {
            TimersRepository.Settings(
                workTime = settings.workTime ?: it.workTime,
                restTime = settings.restTime ?: it.restTime,
                bigRestTime = settings.bigRestTime ?: it.bigRestTime,
                bigRestEnabled = settings.bigRestEnabled ?: it.bigRestEnabled,
                bigRestPer = settings.bigRestPer ?: it.bigRestPer,
                isEveryoneCanPause = settings.isEveryoneCanPause ?: it.isEveryoneCanPause,
                isConfirmationRequired = settings.isConfirmationRequired ?: it.isConfirmationRequired,
                isNotesEnabled = settings.isNotesEnabled ?: it.isNotesEnabled
            )
        }
    }

    override suspend fun addMember(
        userId: UsersRepository.UserId,
        timerId: TimersRepository.TimerId,
        joinTime: UnixTime
    ) {
        timers[timerId.int].members += userId
    }

    override suspend fun removeMember(userId: UsersRepository.UserId, timerId: TimersRepository.TimerId) {
        timers[timerId.int].members.remove(userId)
    }

    override suspend fun getMembers(
        timerId: TimersRepository.TimerId,
        fromUser: UsersRepository.UserId?,
        count: Count
    ): List<UsersRepository.UserId> {
        return timers.getOrNull(timerId.int)?.members ?: emptyList()
    }

    override suspend fun isMemberOf(userId: UsersRepository.UserId, timerId: TimersRepository.TimerId): Boolean {
        return timers.getOrNull(timerId.int)?.members?.contains(userId) == true
    }

    override suspend fun getTimers(
        userId: UsersRepository.UserId,
        fromTimer: TimersRepository.TimerId?,
        count: Count
    ): Sequence<TimersRepository.Timer> {
        return timers.asSequence().filter { it.members.contains(userId) }
            .mapIndexed { i, e -> e.toOriginal(TimersRepository.TimerId(i)) }
    }

    private fun Timer.toOriginal(id: TimersRepository.TimerId): TimersRepository.Timer =
        TimersRepository.Timer(id, name, ownerId, settings)
}