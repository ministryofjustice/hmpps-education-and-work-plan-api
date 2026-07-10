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
import java.time.LocalDate
import java.time.ZoneId

class PefInductionScheduleExtensionDateCalculationServiceTest {

  private val fixedDate = LocalDate.now()
  private val clock = Clock.fixed(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())

  private val inductionScheduleCalculationService = mock<InductionScheduleCalculationService>()

  private val service = PefInductionScheduleDateCalculationService(
    clock,
    inductionScheduleCalculationService,
    ExemptionProperties(DeadlineExtensionRule.ONLY_WHEN_NOT_OVERDUE),
  )

  @Test
  fun `should determine CreateInductionScheduleDto`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val admissionDate = LocalDate.now()
    val prisonId = "BXI"

    given(inductionScheduleCalculationService.getCalculationRuleForNewPrisonAdmission())
      .willReturn(InductionScheduleCalculationRule.NEW_PRISON_ADMISSION_EXTENDED_DEADLINE_PERIOD)

    val expected = aValidCreateInductionScheduleDto(
      prisonNumber = prisonNumber,
      deadlineDate = admissionDate.plusDays(25),
      scheduleCalculationRule = InductionScheduleCalculationRule.NEW_PRISON_ADMISSION_EXTENDED_DEADLINE_PERIOD,
      scheduleStatus = InductionScheduleStatus.SCHEDULED,
      prisonId = prisonId,
    )

    // When
    val actual = service.determineCreateInductionScheduleDto(prisonNumber, admissionDate, prisonId)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
