package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.GoalController
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidUpdateGoalRequest
import java.util.UUID

class UpdateGoalRequestReferenceConstraintValidatorTest {
  private val validatorFactory = Validation.buildDefaultValidatorFactory()
  private val validator = validatorFactory.validator.forExecutables()

  private val goalController = GoalController(mock(), mock())
  private val updateGoalMethod = GoalController::class.java.getMethod(
    "updateGoal",
    UpdateGoalRequest::class.java,
    String::class.java,
    UUID::class.java,
  )

  @Test
  fun `should validate with 0 violations given goalReference matches goalReference in UpdateGoalRequest`() {
    // Then
    val goalReference = aValidReference()
    val updateGoalRequest = aValidUpdateGoalRequest(goalReference = goalReference)

    // When
    val violations: Set<ConstraintViolation<GoalController>> =
      validator.validateParameters(
        goalController,
        updateGoalMethod,
        arrayOf(
          updateGoalRequest,
          null,
          goalReference,
        ),
      )

    // Then
    assertThat(violations).isEmpty()
  }

  @Test
  fun `should validate with 1 violation given goalReference matches goalReference in UpdateGoalRequest`() {
    // Then
    val goalReference = aValidReference()
    val aDifferentGoalReference = aValidReference()
    val updateGoalRequest = aValidUpdateGoalRequest(goalReference = aDifferentGoalReference)

    // When
    val violations: Set<ConstraintViolation<GoalController>> =
      validator.validateParameters(
        goalController,
        updateGoalMethod,
        arrayOf(
          updateGoalRequest,
          null,
          goalReference,
        ),
      )

    // Then
    assertThat(violations).hasSize(1)
    assertThat(violations.first().message)
      .isEqualTo("Goal reference in URI path must match the Goal reference in the request body")
  }
}
