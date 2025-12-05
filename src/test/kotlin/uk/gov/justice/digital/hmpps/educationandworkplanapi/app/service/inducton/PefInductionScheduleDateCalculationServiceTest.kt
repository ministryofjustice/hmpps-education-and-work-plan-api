package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.inducton

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreateInductionScheduleDto
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.InductionExtensionConfig
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class PefInductionScheduleDateCalculationServiceTest {

  private val fixedDate = LocalDate.now()
  private val clock = Clock.fixed(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
  private val service = PefInductionScheduleDateCalculationService(InductionExtensionConfig("2024-12-20:2025-01-01"), clock)

  @Test
  fun `should determine CreateInductionScheduleDto`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val admissionDate = LocalDate.now()
    val prisonId = "BXI"

    val expected = aValidCreateInductionScheduleDto(
      prisonNumber = prisonNumber,
      deadlineDate = admissionDate.plusDays(20),
      scheduleCalculationRule = InductionScheduleCalculationRule.NEW_PRISON_ADMISSION,
      scheduleStatus = InductionScheduleStatus.SCHEDULED,
      prisonId = prisonId,
    )

    // When
    val actual = service.determineCreateInductionScheduleDto(prisonNumber, admissionDate, prisonId)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
