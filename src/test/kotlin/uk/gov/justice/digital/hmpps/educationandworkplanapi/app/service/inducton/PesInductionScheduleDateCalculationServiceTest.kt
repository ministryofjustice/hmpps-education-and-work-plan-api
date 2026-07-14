package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.inducton

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.given
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
  private val inductionScheduleCalculationService = mock<InductionScheduleCalculationService>()
  private val service = PesInductionScheduleDateCalculationService(
    clock,
    inductionScheduleCalculationService,
    ExemptionProperties(DeadlineExtensionRule.ONLY_WHEN_NOT_OVERDUE),
  )

  @Test
  fun `should determine CreateInductionScheduleDto`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val admissionDate = LocalDate.now(clock).minusDays(1)
    val prisonId = "BXI"

    given(inductionScheduleCalculationService.getCalculationRuleForNewPrisonAdmission())
      .willReturn(InductionScheduleCalculationRule.NEW_PRISON_ADMISSION)

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

  @Test
  fun `should determine deadline date for completed assessments as today plus 10 days`() {
    // When
    val actual = service.determineDeadlineDateForCompletedAssessments(InductionScheduleCalculationRule.NEW_PRISON_ADMISSION)

    // Then
    assertThat(actual).isEqualTo(LocalDate.parse("2026-04-27")) // fixed clock is 2026-04-17, plus 10 days
  }

  @Test
  fun `should get the number of extension days for a transfer`() {
    // Given

    // When
    val actual = service.extensionDaysForTransfer()

    // Then
    assertThat(actual).isEqualTo(10)
  }
}
