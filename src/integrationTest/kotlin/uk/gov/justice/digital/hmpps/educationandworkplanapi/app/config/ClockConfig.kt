package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicLong

@Configuration
class ClockConfig(
  @param:Value("\${app.ticking-clock.initial-date-time:}") private val initialDateTime: String?,
) {

  @Bean
  @Primary
  fun testClock(): Clock = if (initialDateTime.isNullOrBlank()) {
    Clock.systemDefaultZone()
  } else {
    val dateTime = LocalDateTime.parse(initialDateTime)
    val zone = ZoneId.systemDefault()
    MillisecondTickingClock(dateTime.atZone(zone).toInstant(), zone)
  }
}

/**
 * An implementation of [Clock] seeded from an initialTime, that returns a time that ticks forward by 1 millisecond each
 * time it is queried.
 * For example, given an initial time of `2026-03-21T09:43:27.127`, the first call to `instant()` will return
 * `2026-03-21T09:43:27.127`, the next will return `2026-03-21T09:43:27.128`, the next will return `2026-03-21T09:43:27.129`
 * and so on, regardless of how much time has passed in reality.
 *
 * This is useful for test scenarios where database entities need to have an incremental timestamp set in a controlled manner.
 *
 * Limitations: If the test scenario is time-sensitive to second precision, seeding the clock with a time at 000 milliseconds
 * will allow for 999 requests for the time before the clock ticks over to the next second.
 * Tests can call `resetMilliSecondsTick()`, perhaps in a BeforeEach method, to reset the time and tick counter back to
 * 0 milliseconds.
 *
 */
class MillisecondTickingClock(
  private var initialTime: Instant,
  private var zone: ZoneId,
  currentTick: Long = 0,
) : Clock() {
  private val currentTick = AtomicLong(currentTick)

  init {
    resetMilliSecondsTick(currentTick)
  }

  override fun getZone() = zone

  override fun withZone(zone: ZoneId): Clock {
    this.zone = zone
    return this
  }

  override fun instant(): Instant = initialTime.plusMillis(getNextTick())

  override fun millis(): Long = instant().toEpochMilli()

  fun resetMilliSecondsTick(tick: Long = 0L) {
    currentTick.set(tick)
    initialTime = initialTime.truncatedTo(ChronoUnit.SECONDS).plusMillis(tick)
  }

  private fun getNextTick() = Duration.ofMillis(currentTick.getAndIncrement()).toMillis()
}
