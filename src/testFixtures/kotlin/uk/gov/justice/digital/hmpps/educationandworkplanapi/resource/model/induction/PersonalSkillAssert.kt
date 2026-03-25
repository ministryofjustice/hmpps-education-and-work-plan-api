package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkill
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkillType

fun assertThat(actual: PersonalSkill?) = PersonalSkillAssert(actual)

/**
 * AssertJ custom assertion for a single [PersonalSkill].
 */
class PersonalSkillAssert(actual: PersonalSkill?) :
  AbstractObjectAssert<PersonalSkillAssert, PersonalSkill?>(
    actual,
    PersonalSkillAssert::class.java,
  ) {

  fun hasSkillType(expected: PersonalSkillType): PersonalSkillAssert {
    isNotNull
    with(actual!!) {
      if (skillType != expected) {
        failWithMessage("Expected skillType to be $expected, but was $skillType")
      }
    }
    return this
  }

  fun hasSkillTypeOther(expected: String): PersonalSkillAssert {
    isNotNull
    with(actual!!) {
      if (skillTypeOther != expected) {
        failWithMessage("Expected skillTypeOther to be $expected, but was $skillTypeOther")
      }
    }
    return this
  }
}
