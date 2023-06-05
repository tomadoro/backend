package io.timemates.backend.timers.types

import com.timemates.backend.validation.createOrThrow
import io.timemates.backend.common.types.value.Count
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

data class TimerSettings(
    val workTime: Duration = 25.minutes,
    val restTime: Duration = 5.minutes,
    val bigRestTime: Duration = 10.minutes,
    val bigRestEnabled: Boolean = true,
    val bigRestPer: Count = Count.createOrThrow(4),
    val isEveryoneCanPause: Boolean = false,
    val isConfirmationRequired: Boolean = false,
) {
    class Patch(
        val workTime: Duration? = null,
        val restTime: Duration? = null,
        val bigRestTime: Duration? = null,
        val bigRestEnabled: Boolean? = null,
        val bigRestPer: Count? = null,
        val isEveryoneCanPause: Boolean? = null,
        val isConfirmationRequired: Boolean? = null,
    )
}