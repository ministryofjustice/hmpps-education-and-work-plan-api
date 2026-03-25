package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GetEmployabilitySkillResponses
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GetEmployabilitySkillsResponse
import java.util.function.Consumer

fun assertThat(actual: GetEmployabilitySkillResponses?) = EmployabilitySkillsAssert(actual)

/**
 * AssertJ custom assertion for a single [GetEmployabilitySkillResponses].
 */
class EmployabilitySkillsAssert(actual: GetEmployabilitySkillResponses?) :
  AbstractObjectAssert<EmployabilitySkillsAssert, GetEmployabilitySkillResponses?>(
    actual,
    EmployabilitySkillsAssert::class.java,
  ) {

  fun hasNumberOfEmployabilitySkills(expected: Int): EmployabilitySkillsAssert {
    isNotNull
    with(actual!!) {
      if (employabilitySkills.size != expected) {
        failWithMessage("Expected number of employability skills to be $expected, but was ${employabilitySkills.size}")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [GetEmployabilitySkillsResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [EmployabilitySkillAssert].
   * Returns this [EmployabilitySkillsAssert] to allow further chained assertions on the parent [GetEmployabilitySkillResponses]
   *
   * The `skillNumber` parameter is not zero indexed to make for better readability in tests. IE. the first skill
   * should be referenced as `.employabilitySkill(1) { .... }`
   */
  fun employabilitySkill(skillNumber: Int, consumer: Consumer<EmployabilitySkillAssert>): EmployabilitySkillsAssert {
    isNotNull
    with(actual!!) {
      val personalSkill = employabilitySkills[skillNumber - 1]
      consumer.accept(assertThat(personalSkill))
    }
    return this
  }
}
