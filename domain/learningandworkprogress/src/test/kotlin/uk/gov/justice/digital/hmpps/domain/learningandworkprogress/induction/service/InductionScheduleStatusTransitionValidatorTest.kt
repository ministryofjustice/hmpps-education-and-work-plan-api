package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus.COMPLETED
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus.EXEMPT_PRISONER_DEATH
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus.EXEMPT_PRISONER_OTHER_HEALTH_ISSUES
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus.EXEMPT_PRISON_STAFF_REDEPLOYMENT
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InvalidInductionScheduleStatusException

class InductionScheduleStatusTransitionValidatorTest {

  private val validator = InductionScheduleStatusTransitionValidator()

  @ParameterizedTest
  @MethodSource("transitionTestCases")
  fun `validate handles transitions correctly`(testCase: TransitionTestCase) {
    if (testCase.isValid) {
      assertDoesNotThrow {
        validator.validate(testCase.prisonNumber, testCase.currentStatus, testCase.newStatus)
      }
    } else {
      val exception = assertThrows<InvalidInductionScheduleStatusException> {
        validator.validate(testCase.prisonNumber, testCase.currentStatus, testCase.newStatus)
      }

      // Optionally validate exception details
      assert(exception.message!!.contains(testCase.currentStatus.name))
      assert(exception.message!!.contains(testCase.newStatus.name))
    }
  }

  companion object {

    private const val PRISON_NUMBER = "A4321AB"

    @JvmStatic
    fun transitionTestCases(): List<TransitionTestCase> = listOf(
      TransitionTestCase(
        prisonNumber = PRISON_NUMBER,
        currentStatus = COMPLETED,
        newStatus = SCHEDULED,
        reason = "Cannot transition from COMPLETED to any other status.",
        isValid = false,
      ),
      TransitionTestCase(
        prisonNumber = PRISON_NUMBER,
        currentStatus = EXEMPT_PRISONER_OTHER_HEALTH_ISSUES,
        newStatus = EXEMPT_PRISON_STAFF_REDEPLOYMENT,
        reason = "Cannot transition from one exemption or exclusion status to another.",
        isValid = false,
      ),
      TransitionTestCase(
        prisonNumber = PRISON_NUMBER,
        currentStatus = SCHEDULED,
        newStatus = COMPLETED,
        reason = "Cannot transition to restricted statuses using this route.",
        isValid = false,
      ),
      TransitionTestCase(
        prisonNumber = PRISON_NUMBER,
        currentStatus = EXEMPT_PRISONER_DEATH,
        newStatus = COMPLETED,
        reason = "Cannot transition to restricted statuses using this route.",
        isValid = false,
      ),
      TransitionTestCase(
        prisonNumber = PRISON_NUMBER,
        currentStatus = EXEMPT_PRISONER_DEATH,
        newStatus = PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS,
        reason = "Cannot transition to restricted statuses using this route.",
        isValid = false,
      ),
      TransitionTestCase(
        prisonNumber = PRISON_NUMBER,
        currentStatus = SCHEDULED,
        newStatus = EXEMPT_PRISON_STAFF_REDEPLOYMENT,
        isValid = true,
      ),
      TransitionTestCase(
        prisonNumber = PRISON_NUMBER,
        currentStatus = EXEMPT_PRISON_STAFF_REDEPLOYMENT,
        newStatus = SCHEDULED,
        isValid = true,
      ),
      TransitionTestCase(
        prisonNumber = PRISON_NUMBER,
        currentStatus = SCHEDULED,
        newStatus = SCHEDULED,
        isValid = true,
      ),
    )
  }

  data class TransitionTestCase(
    val prisonNumber: String,
    val currentStatus: InductionScheduleStatus,
    val newStatus: InductionScheduleStatus,
    val isValid: Boolean,
    val reason: String? = null,
  )
}
