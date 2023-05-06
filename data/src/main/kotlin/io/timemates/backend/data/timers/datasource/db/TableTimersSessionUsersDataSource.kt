package io.timemates.backend.data.timers.datasource.db

import io.timemates.backend.data.timers.datasource.db.entities.DbSessionUser
import io.timemates.backend.data.timers.datasource.db.mappers.TimerSessionMapper
import io.timemates.backend.data.timers.datasource.db.tables.TimersSessionUsersTable
import io.timemates.backend.exposed.suspendedTransaction
import io.timemates.backend.exposed.upsert
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class TableTimersSessionUsersDataSource(
    private val database: Database,
    private val timerSessionMapper: TimerSessionMapper,
) {
    init {
        transaction(database) {
            SchemaUtils.create(TimersSessionUsersTable)
        }
    }

    suspend fun isAnyUserActiveAfter(
        timerId: Long, afterTime: Long,
    ): Boolean = suspendedTransaction(database) {
        TimersSessionUsersTable.select {
            TimersSessionUsersTable.TIMER_ID eq timerId and
                (TimersSessionUsersTable.LAST_ACTIVITY_TIME greater afterTime)
        }.empty().not()
    }

    suspend fun getUsers(
        timerId: Long,
        afterTime: Long,
    ): List<DbSessionUser> = suspendedTransaction(database) {
        TimersSessionUsersTable.select {
            TimersSessionUsersTable.TIMER_ID eq timerId and
                (TimersSessionUsersTable.LAST_ACTIVITY_TIME greater afterTime)
        }.map(timerSessionMapper::resultRowToSessionUser)
    }

    suspend fun assignUser(
        timerId: Long,
        userId: Long,
        isConfirmed: Boolean,
        lastActivityTime: Long,
    ): Unit = suspendedTransaction(database) {
        val condition = Op.build {
            TimersSessionUsersTable.TIMER_ID eq timerId and
                (TimersSessionUsersTable.USER_ID eq userId)
        }

        TimersSessionUsersTable.upsert(condition = condition) { statement, _ ->
            statement[TIMER_ID] = timerId
            statement[USER_ID] = userId
            statement[IS_CONFIRMED] = isConfirmed
            statement[LAST_ACTIVITY_TIME] = lastActivityTime
        }
    }

    suspend fun unassignUser(
        timerId: Long,
        userId: Long,
    ): Unit = suspendedTransaction(database) {
        TimersSessionUsersTable.deleteWhere { TIMER_ID eq timerId and (USER_ID eq userId) }
    }
}