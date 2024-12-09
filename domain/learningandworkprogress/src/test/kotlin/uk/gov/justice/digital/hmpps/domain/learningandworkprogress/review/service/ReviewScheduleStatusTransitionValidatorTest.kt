package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.InvalidReviewScheduleStatusException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.COMPLETED
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.EXEMPT_PRISONER_DEATH
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.EXEMPT_PRISONER_OTHER_HEALTH_ISSUES
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.EXEMPT_PRISON_STAFF_REDEPLOYMENT
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.SCHEDULED

class ReviewScheduleStatusTransitionValidatorTest {

  private val validator = ReviewScheduleStatusTransitionValidator()

  @ParameterizedTest
  @MethodSource("transitionTestCases")
  fun `validate handles transitions correctly`(testCase: TransitionTestCase) {
    if (testCase.isValid) {
      assertDoesNotThrow {
        validator.validate(testCase.prisonNumber, testCase.currentStatus, testCase.newStatus)
      }
    } else {
      val exception = assertThrows<InvalidReviewScheduleStatusException> {
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
        newStatus = SCHEDULED,
        reason = "Can only transition to SCHEDULED if the current status is an exemption or exclusion.",
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
    )
  }

  data class TransitionTestCase(
    val prisonNumber: String,
    val currentStatus: ReviewScheduleStatus,
    val newStatus: ReviewScheduleStatus,
    val isValid: Boolean,
    val reason: String? = null,
  )
}
