package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator

import jakarta.validation.ConstraintValidatorContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import java.time.LocalDate

class NotInPastValidatorTest {

  private val validator = NotInPastValidator()

  @ParameterizedTest
  @MethodSource("dates")
  fun `should validate given date`(date: LocalDate, expected: Boolean) {
    // Given
    validator.initialize(NotInPast())

    // When
    val actual = validator.isValid(date, mock(ConstraintValidatorContext::class.java))

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  companion object {
    @JvmStatic
    fun dates(): List<Arguments> {
      val now = LocalDate.now()
      return listOf(
        Arguments.of(now, true),
        Arguments.of(now.plusDays(1), true),
        Arguments.of(now.plusYears(1), true),
        Arguments.of(now.minusDays(1), false),
        Arguments.of(now.minusYears(1), false),
      )
    }
  }
}
