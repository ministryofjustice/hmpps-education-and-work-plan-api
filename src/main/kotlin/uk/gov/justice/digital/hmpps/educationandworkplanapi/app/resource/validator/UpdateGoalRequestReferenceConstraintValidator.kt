package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import jakarta.validation.constraintvalidation.SupportedValidationTarget
import jakarta.validation.constraintvalidation.ValidationTarget
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateGoalRequest
import java.util.UUID
import kotlin.reflect.KClass

/**
 * Hibernate Constraint Validator class and [] annotation ta validate that the `goalReference`
 * in the `UpdateGoalRequest` matches the `goalReference` in the REST request URI path.
 */
@SupportedValidationTarget(ValidationTarget.PARAMETERS)
class UpdateGoalRequestReferenceConstraintValidator : ConstraintValidator<GoalReferenceMatchesReferenceInUpdateGoalRequest, Array<Any>> {

  override fun isValid(
    methodArguments: Array<Any>,
    context: ConstraintValidatorContext,
  ): Boolean {
    val updateGoalRequest: UpdateGoalRequest = methodArguments[0] as UpdateGoalRequest
    val goalReference: UUID = methodArguments[2] as UUID
    return updateGoalRequest.goalReference == goalReference
  }
}

@Constraint(validatedBy = [UpdateGoalRequestReferenceConstraintValidator::class])
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class GoalReferenceMatchesReferenceInUpdateGoalRequest(
  val message: String = "Goal reference in URI path must match the Goal reference in the request body",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)
