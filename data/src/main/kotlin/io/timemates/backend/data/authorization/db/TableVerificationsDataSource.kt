package io.timemates.backend.data.authorization.db

import com.timemates.backend.time.UnixTime
import io.timemates.backend.authorization.types.value.Attempts
import io.timemates.backend.authorization.types.value.VerificationCode
import io.timemates.backend.authorization.types.value.VerificationHash
import io.timemates.backend.data.authorization.db.entities.DbVerification
import io.timemates.backend.data.authorization.db.mapper.DbVerificationsMapper
import io.timemates.backend.data.authorization.db.table.VerificationSessionsTable
import io.timemates.backend.data.authorization.db.table.VerificationSessionsTable.ATTEMPTS
import io.timemates.backend.data.authorization.db.table.VerificationSessionsTable.VERIFICATION_HASH
import io.timemates.backend.exposed.suspendedTransaction
import io.timemates.backend.exposed.update
import io.timemates.backend.users.types.value.EmailAddress
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class TableVerificationsDataSource(
    private val database: Database,
    private val verificationsMapper: DbVerificationsMapper,
) {
    suspend fun add(
        emailAddress: String,
        verificationToken: String,
        code: String,
        time: Long,
        attempts: Int,
    ): Unit = suspendedTransaction(database) {
        VerificationSessionsTable.insert {
            it[EMAIL] = emailAddress
            it[VERIFICATION_HASH] = verificationToken
            it[IS_CONFIRMED] = false
            it[CONFIRMATION_CODE] = code
            it[ATTEMPTS] = attempts
            it[INIT_TIME] = time
        }
    }

    suspend fun getVerification(verificationHash: String): DbVerification? = suspendedTransaction(database) {
        VerificationSessionsTable.select { VERIFICATION_HASH eq verificationHash }
            .singleOrNull()
            ?.let(verificationsMapper::resultRowToDbVerification)
    }

    suspend fun decreaseAttempts(verificationHash: String): Unit = suspendedTransaction(database) {
        val current = VerificationSessionsTable.select { VERIFICATION_HASH eq verificationHash }
            .singleOrNull()
            ?.get(ATTEMPTS)
            ?: error("Cannot decrease number of attempts, as there is no such verification session")

        VerificationSessionsTable.update(VERIFICATION_HASH eq verificationHash) {
            it[ATTEMPTS] = current - 1
        }
    }

    suspend fun getAttempts(email: String, afterTime: Long): Int = suspendedTransaction(database) {
        VerificationSessionsTable.select {
            VerificationSessionsTable.EMAIL eq email and
                (VerificationSessionsTable.INIT_TIME greater afterTime)
        }.sumOf { it[VerificationSessionsTable.ATTEMPTS] }
    }

    suspend fun getSessionsCount(
        email: String,
        afterTime: Long,
    ): Int = suspendedTransaction(database) {
        VerificationSessionsTable.select {
            VerificationSessionsTable.EMAIL eq email and
                (VerificationSessionsTable.INIT_TIME greater afterTime)
        }.count().toInt()
    }

    suspend fun setAsConfirmed(verificationHash: String): Boolean = suspendedTransaction(database) {
        VerificationSessionsTable.update(VERIFICATION_HASH eq verificationHash) {
            it[IS_CONFIRMED] = true
        } > 0
    }

    suspend fun remove(verificationHash: String): Unit = suspendedTransaction(database) {
        VerificationSessionsTable.deleteWhere { VERIFICATION_HASH eq verificationHash }
    }
}