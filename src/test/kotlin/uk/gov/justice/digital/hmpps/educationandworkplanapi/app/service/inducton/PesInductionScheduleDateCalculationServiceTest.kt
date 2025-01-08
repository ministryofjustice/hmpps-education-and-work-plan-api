package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.inducton

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreateInductionScheduleDto
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import java.time.LocalDate

class PesInductionScheduleDateCalculationServiceTest {
  private val service = PesInductionScheduleDateCalculationService()

  @Test
  fun `should determine CreateInductionScheduleDto`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val admissionDate = LocalDate.now().minusDays(1)

    val expected = aValidCreateInductionScheduleDto(
      prisonNumber = prisonNumber,
      deadlineDate = LocalDate.now(),
      scheduleCalculationRule = InductionScheduleCalculationRule.NEW_PRISON_ADMISSION,
      scheduleStatus = InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS,
    )

    // When
    val actual = service.determineCreateInductionScheduleDto(prisonNumber, admissionDate)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
