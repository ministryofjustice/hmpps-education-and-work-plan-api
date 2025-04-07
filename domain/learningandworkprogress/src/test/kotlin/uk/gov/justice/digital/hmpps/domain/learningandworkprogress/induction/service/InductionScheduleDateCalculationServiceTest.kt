package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionScheduleDto
import java.time.LocalDate

class InductionScheduleDateCalculationServiceTest {
  companion object {
    private val TODAY = LocalDate.now()
  }

  // New up an anonymous instance of the abstract class to test the methods implemented in the abstract class
  // The implementations of determineCreateInductionScheduleDto are tested in the concrete implementations of InductionScheduleDateCalculationService
  private val dateCalculationService = object : InductionScheduleDateCalculationService() {
    override fun determineCreateInductionScheduleDto(
      prisonNumber: String,
      admissionDate: LocalDate,
      prisonId: String,
      newAdmission: Boolean,
      releaseDate: LocalDate?,
      dataCorrection: Boolean,
    ): CreateInductionScheduleDto {
      TODO("Not implemented here")
    }
  }

  @Nested
  inner class CalculateAdjustedInductionDueDate {
    @ParameterizedTest
    @CsvSource(
      value = [
        "EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY",
        "EXEMPT_PRISONER_OTHER_HEALTH_ISSUES",
        "EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF",
        "EXEMPT_PRISONER_SAFETY_ISSUES",
        "EXEMPT_PRISON_REGIME_CIRCUMSTANCES",
      ],
    )
    fun `should calculate adjusted induction due date given an exemption status that is classed as an exclusion and the induction due date is later than the calculated date`(
      scheduleStatus: InductionScheduleStatus,
    ) {
      // Given
      val inductionSchedule = aValidInductionSchedule(
        deadlineDate = TODAY.plusDays(11),
        scheduleStatus = scheduleStatus,
      )

      val expectedReviewDate = TODAY.plusDays(11)

      // When
      val actual = dateCalculationService.calculateAdjustedInductionDueDate(inductionSchedule)

      // Then
      assertThat(actual).isEqualTo(expectedReviewDate)
    }

    @ParameterizedTest
    @CsvSource(
      value = [
        "EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY",
        "EXEMPT_PRISONER_OTHER_HEALTH_ISSUES",
        "EXEMPT_SECURITY_ISSUE_RISK_TO_STAFF",
        "EXEMPT_PRISONER_SAFETY_ISSUES",
        "EXEMPT_PRISON_REGIME_CIRCUMSTANCES",
      ],
    )
    fun `should calculate adjusted induction due date given an exemption status that is classed as an exclusion and the induction due date is earlier than the calculated date`(
      scheduleStatus: InductionScheduleStatus,
    ) {
      // Given
      val inductionSchedule = aValidInductionSchedule(
        deadlineDate = TODAY.plusDays(9),
        scheduleStatus = scheduleStatus,
      )

      val expectedReviewDate = TODAY.plusDays(10)

      // When
      val actual = dateCalculationService.calculateAdjustedInductionDueDate(inductionSchedule)

      // Then
      assertThat(actual).isEqualTo(expectedReviewDate)
    }

    @ParameterizedTest
    @CsvSource(
      value = [
        "EXEMPT_PRISONER_FAILED_TO_ENGAGE",
        "EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED",
        "EXEMPT_PRISON_STAFF_REDEPLOYMENT",
        "EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE",
        "EXEMPT_PRISONER_TRANSFER",
        "EXEMPT_PRISONER_RELEASE",
        "EXEMPT_PRISONER_DEATH",
        "EXEMPT_PRISONER_MERGE",
        "EXEMPT_SCREENING_AND_ASSESSMENT_IN_PROGRESS",
        "EXEMPT_SCREENING_AND_ASSESSMENT_INCOMPLETE",
      ],
    )
    fun `should calculate adjusted induction due date given an exemption status that is classed as an exemption and the induction due date is later than the calculated date`(
      scheduleStatus: InductionScheduleStatus,
    ) {
      // Given
      val inductionSchedule = aValidInductionSchedule(
        deadlineDate = TODAY.plusDays(6),
        scheduleStatus = scheduleStatus,
      )

      val expectedReviewDate = TODAY.plusDays(6)

      // When
      val actual = dateCalculationService.calculateAdjustedInductionDueDate(inductionSchedule)

      // Then
      assertThat(actual).isEqualTo(expectedReviewDate)
    }

    @ParameterizedTest
    @CsvSource(
      value = [
        "EXEMPT_PRISONER_FAILED_TO_ENGAGE",
        "EXEMPT_PRISONER_ESCAPED_OR_ABSCONDED",
        "EXEMPT_PRISON_STAFF_REDEPLOYMENT",
        "EXEMPT_PRISON_OPERATION_OR_SECURITY_ISSUE",
        "EXEMPT_PRISONER_TRANSFER",
        "EXEMPT_PRISONER_RELEASE",
        "EXEMPT_PRISONER_DEATH",
        "EXEMPT_PRISONER_MERGE",
      ],
    )
    fun `should calculate adjusted induction due date given an exemption status that is classed as an exemption and the induction due date is earlier than the calculated date`(
      scheduleStatus: InductionScheduleStatus,
    ) {
      // Given
      val inductionSchedule = aValidInductionSchedule(
        deadlineDate = TODAY.plusDays(4),
        scheduleStatus = scheduleStatus,
      )

      val expectedReviewDate = TODAY.plusDays(5)

      // When
      val actual = dateCalculationService.calculateAdjustedInductionDueDate(inductionSchedule)

      // Then
      assertThat(actual).isEqualTo(expectedReviewDate)
    }

    @ParameterizedTest
    @CsvSource(
      value = [
        "SCHEDULED",
        "COMPLETED",
      ],
    )
    fun `should calculate adjusted induction due date given a status that not an exemption status`(
      scheduleStatus: InductionScheduleStatus,
    ) {
      // Given
      val inductionSchedule = aValidInductionSchedule(
        deadlineDate = TODAY.plusDays(1),
        scheduleStatus = scheduleStatus,
      )

      val expectedReviewDate = TODAY.plusDays(1)

      // When
      val actual = dateCalculationService.calculateAdjustedInductionDueDate(inductionSchedule)

      // Then
      assertThat(actual).isEqualTo(expectedReviewDate)
    }

    @Test
    fun `should calculate adjusted induction due date given EXEMPT_SYSTEM_TECHNICAL_ISSUE and the induction due date is later than the calculated date`() {
      // Given
      val inductionSchedule = aValidInductionSchedule(
        deadlineDate = TODAY.plusDays(6),
        scheduleStatus = InductionScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE,
      )

      val expectedReviewDate = TODAY.plusDays(6)

      // When
      val actual = dateCalculationService.calculateAdjustedInductionDueDate(inductionSchedule)

      // Then
      assertThat(actual).isEqualTo(expectedReviewDate)
    }

    @Test
    fun `should calculate adjusted induction due date given EXEMPT_SYSTEM_TECHNICAL_ISSUE and the induction due date is earlier than the calculated date`() {
      // Given
      val inductionSchedule = aValidInductionSchedule(
        deadlineDate = TODAY.plusDays(4),
        scheduleStatus = InductionScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE,
      )

      val expectedReviewDate = TODAY.plusDays(5)

      // When
      val actual = dateCalculationService.calculateAdjustedInductionDueDate(inductionSchedule)

      // Then
      assertThat(actual).isEqualTo(expectedReviewDate)
    }
  }
}
