package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.inducton

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreateInductionScheduleDto
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.DeadlineExtensionRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.ExemptionProperties
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class PesInductionScheduleDateCalculationServiceTest {
  private val fixedTimestamp = Instant.parse("2026-04-17T09:13:22.123Z")
  private val clock = Clock.fixed(fixedTimestamp, ZoneId.of("UTC"))
  private val service = PesInductionScheduleDateCalculationService(
    ExemptionProperties(DeadlineExtensionRule.ONLY_WHEN_NOT_OVERDUE),
    clock,
  )

  @Test
  fun `should determine CreateInductionScheduleDto`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val admissionDate = LocalDate.now(clock).minusDays(1)
    val prisonId = "BXI"

    val expected = aValidCreateInductionScheduleDto(
      prisonNumber = prisonNumber,
      deadlineDate = LocalDate.parse("2026-04-17"),
      scheduleCalculationRule = InductionScheduleCalculationRule.NEW_PRISON_ADMISSION,
      scheduleStatus = InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS,
    )

    // When
    val actual = service.determineCreateInductionScheduleDto(prisonNumber, admissionDate, prisonId)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
