package io.timemates.backend.authorization.usecases

import com.timemates.backend.time.TimeProvider
import com.timemates.backend.time.UnixTime
import com.timemates.backend.validation.createOrThrow
import com.timemates.random.RandomProvider
import io.timemates.backend.authorization.repositories.EmailsRepository
import io.timemates.backend.authorization.repositories.VerificationsRepository
import io.timemates.backend.authorization.types.Email
import io.timemates.backend.authorization.types.value.Attempts
import io.timemates.backend.authorization.types.value.VerificationCode
import io.timemates.backend.authorization.types.value.VerificationHash
import io.timemates.backend.users.types.value.EmailAddress
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class AuthByEmailUseCase(
    private val emails: EmailsRepository,
    private val verifications: VerificationsRepository,
    private val timeProvider: TimeProvider,
    private val randomProvider: RandomProvider,
) {
    suspend fun execute(emailAddress: EmailAddress): Result {
        // used for limits (max count of sessions & attempts that can be requested)
        val sessionsTimeBoundary = timeProvider.provide() - 1.hours

        return when {
            verifications.getNumberOfAttempts(emailAddress, sessionsTimeBoundary) >= 9 ||
                verifications.getNumberOfSessions(emailAddress, sessionsTimeBoundary) >= 3 ->
                Result.AttemptsExceed

            else -> {
                val code = VerificationCode.createOrThrow(randomProvider.randomHash(VerificationCode.SIZE))
                val verificationHash = VerificationHash.createOrThrow(randomProvider.randomHash(VerificationHash.SIZE))
                val expiresAt = timeProvider.provide() + 10.minutes
                val totalAttempts = Attempts.createOrThrow(3)

                if(!emails.send(emailAddress, Email.AuthorizeEmail(emailAddress, code)))
                    return Result.SendFailed
                verifications.save(emailAddress, verificationHash, code, expiresAt, totalAttempts)
                Result.Success(timeProvider.provide() + 10.minutes, totalAttempts)
            }
        }
    }

    enum class Test(val int: Int) {

    }

    sealed interface Result {
        data object SendFailed : Result
        data class Success(
            val expiresAt: UnixTime,
            val attempts: Attempts,
        ) : Result

        data object AttemptsExceed : Result
    }
}