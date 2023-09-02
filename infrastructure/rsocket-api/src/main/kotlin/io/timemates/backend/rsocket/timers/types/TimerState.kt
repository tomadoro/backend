package io.timemates.backend.rsocket.timers.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents current timer state.
 * @see <a href="https://github.com/timemates/sdk/blob/master/sdk/src/commonMain/kotlin/io/timemates/sdk/timers/types/Timer.kt#L27">SDK Source</a>
 */
@Serializable
sealed class TimerState {
    abstract val endsAt: Long?
    abstract val publishTime: Long

    @SerialName("inactive")
    data class Inactive(override val publishTime: Long) : TimerState() {
        override val endsAt: Long? get() = null
    }

    @SerialName("pause")
    data class Pause(override val publishTime: Long) : TimerState() {
        override val endsAt: Long? get() = null
    }

    @SerialName("running")
    data class Running(override val endsAt: Long, override val publishTime: Long) : TimerState()

    @SerialName("rest")
    data class Rest(override val endsAt: Long, override val publishTime: Long) : TimerState()

    @SerialName("confirmation")
    data class Confirmation(override val endsAt: Long, override val publishTime: Long) : TimerState()
}