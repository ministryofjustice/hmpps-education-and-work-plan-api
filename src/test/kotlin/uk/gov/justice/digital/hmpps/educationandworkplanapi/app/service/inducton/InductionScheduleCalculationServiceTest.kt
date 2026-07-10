package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.inducton

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.InductionExtensionConfig
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class InductionScheduleCalculationServiceTest {

  private val fixedDate = LocalDate.now()
  private val clock = Clock.fixed(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())

  @Test
  fun `should get calculation rule for new admission given today is in the extended deadline period`() {
    // Given
    val inductionExtensionConfig = InductionExtensionConfig(
      "${LocalDate.now().minusDays(1)}:${LocalDate.now().plusDays(1)}",
    )

    val service = InductionScheduleCalculationService(
      inductionExtensionConfig,
      clock,
    )

    // When
    val actual = service.getCalculationRuleForNewPrisonAdmission()

    // Then
    assertThat(actual).isEqualTo(InductionScheduleCalculationRule.NEW_PRISON_ADMISSION_EXTENDED_DEADLINE_PERIOD)
  }

  @Test
  fun `should get calculation rule for new admission given today is not in the extended deadline period`() {
    // Given
    val inductionExtensionConfig = InductionExtensionConfig(
      "${LocalDate.now().plusDays(1)}:${LocalDate.now().plusDays(10)}",
    )

    val service = InductionScheduleCalculationService(
      inductionExtensionConfig,
      clock,
    )

    // When
    val actual = service.getCalculationRuleForNewPrisonAdmission()

    // Then
    assertThat(actual).isEqualTo(InductionScheduleCalculationRule.NEW_PRISON_ADMISSION)
  }
}
