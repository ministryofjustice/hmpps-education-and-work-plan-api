package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.time.LocalDate
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NotInPastValidator::class])
annotation class NotInPast(
  val message: String = "Cannot be in the past",
  val required: Boolean = false,
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)

class NotInPastValidator : ConstraintValidator<NotInPast, LocalDate> {
  private lateinit var constraintAnnotation: NotInPast

  override fun initialize(constraintAnnotation: NotInPast) {
    this.constraintAnnotation = constraintAnnotation
  }

  override fun isValid(date: LocalDate?, context: ConstraintValidatorContext): Boolean = date?.isNotInThePast() ?: true

  private fun LocalDate.isNotInThePast() = !this.isBefore(LocalDate.now())
}
