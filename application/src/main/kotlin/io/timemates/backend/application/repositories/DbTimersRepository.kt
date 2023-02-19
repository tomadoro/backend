package io.timemates.backend.application.repositories

import io.timemates.backend.types.value.Count
import io.timemates.backend.types.value.UnixTime
import io.timemates.backend.types.value.Milliseconds
import io.timemates.backend.types.value.TimerName
import io.timemates.backend.repositories.TimersRepository
import io.timemates.backend.repositories.UsersRepository
import io.timemates.backend.integrations.postgresql.repositories.datasource.TimersDatabaseDataSource

class DbTimersRepository(
    private val datasource: TimersDatabaseDataSource
) : TimersRepository {
    override suspend fun createTimer(
        name: TimerName,
        settings: TimersRepository.Settings,
        ownerId: UsersRepository.UserId,
        creationTime: UnixTime
    ): TimersRepository.TimerId {
        return TimersRepository.TimerId(
            datasource.createTimer(
                name.string,
                creationTime.long,
                ownerId.int,
                settings.toInternalSettings()
            )
        )
    }

    override suspend fun getTimer(timerId: TimersRepository.TimerId): TimersRepository.Timer? {
        return datasource.getTimerById(timerId.int)?.toExternalTimer()
    }

    override suspend fun removeTimer(timerId: TimersRepository.TimerId) {
        datasource.removeTimer(timerId.int)
    }

    override suspend fun getOwnedTimersCount(ownerId: UsersRepository.UserId, after: UnixTime): Int {
        return datasource.getCountOfTimers(ownerId.int, after.long).toInt()
    }

    override suspend fun getTimerSettings(timerId: TimersRepository.TimerId): TimersRepository.Settings? {
        return datasource.getSettings(timerId.int)?.toExternalSettings()
    }

    override suspend fun setTimerSettings(timerId: TimersRepository.TimerId, settings: TimersRepository.NewSettings) {
        datasource.setNewSettings(timerId.int, settings.toInternalPatchable())
    }

    override suspend fun addMember(
        userId: UsersRepository.UserId,
        timerId: TimersRepository.TimerId,
        joinTime: UnixTime
    ) {
        datasource.addMember(timerId.int, userId.int, joinTime.long)
    }

    override suspend fun removeMember(userId: UsersRepository.UserId, timerId: TimersRepository.TimerId) {
        datasource.removeMember(userId.int, timerId.int)
    }

    override suspend fun getMembers(
        timerId: TimersRepository.TimerId,
        fromUser: UsersRepository.UserId?,
        count: Count
    ): List<UsersRepository.UserId> {
        return datasource.getMembersIds(timerId.int, fromUser?.int ?: 0, count.int)
            .map { UsersRepository.UserId(it) }
            .toList()
    }

    override suspend fun isMemberOf(userId: UsersRepository.UserId, timerId: TimersRepository.TimerId): Boolean {
        return datasource.isMemberOf(timerId.int, userId.int)
    }

    override suspend fun getTimers(
        userId: UsersRepository.UserId,
        fromTimer: TimersRepository.TimerId?,
        count: Count
    ): Sequence<TimersRepository.Timer> {
        return datasource.getUserTimers(userId.int, fromTimer?.int ?: Int.MAX_VALUE, count.int)
            .map { it.toExternalTimer() }
    }

    private fun TimersDatabaseDataSource.Timer.toExternalTimer(): TimersRepository.Timer {
        return TimersRepository.Timer(
            TimersRepository.TimerId(id),
            TimerName(timerName),
            UsersRepository.UserId(ownerId),
            settings.toExternalSettings()
        )
    }

    private fun TimersDatabaseDataSource.Timer.Settings.toExternalSettings(): TimersRepository.Settings {
        return TimersRepository.Settings(
            Milliseconds(workTime), Milliseconds(restTime), Milliseconds(bigRestTime), bigRestEnabled,
            Count(bigRestPer), isEveryoneCanPause, isConfirmationRequired, isNotesEnabled
        )
    }

    private fun TimersRepository.NewSettings.toInternalPatchable(): TimersDatabaseDataSource.Timer.Settings.Patchable {
        return TimersDatabaseDataSource.Timer.Settings.Patchable(
            workTime?.long,
            restTime?.long,
            bigRestTime?.long,
            bigRestEnabled,
            bigRestPer?.int,
            isEveryoneCanPause,
            isConfirmationRequired,
            isNotesEnabled
        )
    }

    private fun TimersRepository.Settings.toInternalSettings(): TimersDatabaseDataSource.Timer.Settings {
        return TimersDatabaseDataSource.Timer.Settings(
            workTime.long,
            restTime.long,
            bigRestTime.long,
            bigRestEnabled,
            bigRestPer.int,
            isEveryoneCanPause,
            isConfirmationRequired,
            isNotesEnabled
        )
    }
}