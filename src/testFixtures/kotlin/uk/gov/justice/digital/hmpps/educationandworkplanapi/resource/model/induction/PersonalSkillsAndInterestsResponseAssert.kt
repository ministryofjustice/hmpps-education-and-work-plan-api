package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.isBeforeRounded
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkill
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkillsAndInterestsResponse
import java.time.OffsetDateTime
import java.util.UUID
import java.util.function.Consumer

fun assertThat(actual: PersonalSkillsAndInterestsResponse?) = PersonalSkillsAndInterestsResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [PersonalSkillsAndInterestsResponse].
 */
class PersonalSkillsAndInterestsResponseAssert(actual: PersonalSkillsAndInterestsResponse?) :
  AbstractObjectAssert<PersonalSkillsAndInterestsResponseAssert, PersonalSkillsAndInterestsResponse?>(
    actual,
    PersonalSkillsAndInterestsResponseAssert::class.java,
  ) {

  fun hasReference(expected: UUID): PersonalSkillsAndInterestsResponseAssert {
    isNotNull
    with(actual!!) {
      if (reference != expected) {
        failWithMessage("Expected reference to be $expected, but was $reference")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: OffsetDateTime): PersonalSkillsAndInterestsResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedAtOrAfter(dateTime: OffsetDateTime): PersonalSkillsAndInterestsResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAt.isBeforeRounded(dateTime)) {
        failWithMessage("Expected createdAt to be after $dateTime, but was $createdAt")
      }
    }
    return this
  }

  fun wasUpdatedAt(expected: OffsetDateTime): PersonalSkillsAndInterestsResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt != expected) {
        failWithMessage("Expected updatedAt to be $expected, but was $updatedAt")
      }
    }
    return this
  }

  fun wasUpdatedAtOrAfter(dateTime: OffsetDateTime): PersonalSkillsAndInterestsResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt.isBeforeRounded(dateTime)) {
        failWithMessage("Expected updatedAt to be after $dateTime, but was $updatedAt")
      }
    }
    return this
  }

  fun wasCreatedBy(expected: String): PersonalSkillsAndInterestsResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun wasCreatedByDisplayName(expected: String): PersonalSkillsAndInterestsResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdByDisplayName != expected) {
        failWithMessage("Expected createdByDisplayName to be $expected, but was $createdByDisplayName")
      }
    }
    return this
  }

  fun wasUpdatedBy(expected: String): PersonalSkillsAndInterestsResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedBy != expected) {
        failWithMessage("Expected updatedBy to be $expected, but was $updatedBy")
      }
    }
    return this
  }

  fun wasUpdatedByDisplayName(expected: String): PersonalSkillsAndInterestsResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedByDisplayName != expected) {
        failWithMessage("Expected updatedByDisplayName to be $expected, but was $updatedByDisplayName")
      }
    }
    return this
  }

  fun wasCreatedAtPrison(expected: String): PersonalSkillsAndInterestsResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAtPrison != expected) {
        failWithMessage("Expected createdAtPrison to be $expected, but was $createdAtPrison")
      }
    }
    return this
  }

  fun wasUpdatedAtPrison(expected: String): PersonalSkillsAndInterestsResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAtPrison != expected) {
        failWithMessage("Expected updatedAtPrison to be $expected, but was $updatedAtPrison")
      }
    }
    return this
  }

  fun hasNoPersonalInterests(): PersonalSkillsAndInterestsResponseAssert {
    isNotNull
    with(actual!!) {
      if (interests.isNotEmpty()) {
        failWithMessage("Expected no personal interests, but was ${interests.size}")
      }
    }
    return this
  }

  fun hasNumberOfPersonalInterests(expected: Int): PersonalSkillsAndInterestsResponseAssert {
    isNotNull
    with(actual!!) {
      if (interests.size != expected) {
        failWithMessage("Expected number of personal interests to be $expected, but was ${interests.size}")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [PersonalInterest]. Takes a lambda as the method argument
   * to call assertion methods provided by [PersonalInterestAssert].
   * Returns this [PersonalSkillsAndInterestsResponseAssert] to allow further chained assertions on the parent [PersonalSkillsAndInterestsResponse]
   *
   * The `interestNumber` parameter is not zero indexed to make for better readability in tests. IE. the first interest
   * should be referenced as `.personalInterest(1) { .... }`
   */
  fun personalInterest(interestNumber: Int, consumer: Consumer<PersonalInterestAssert>): PersonalSkillsAndInterestsResponseAssert {
    isNotNull
    with(actual!!) {
      val personalInterest = interests[interestNumber - 1]
      consumer.accept(assertThat(personalInterest))
    }
    return this
  }

  fun hasNoPersonalSkills(): PersonalSkillsAndInterestsResponseAssert {
    isNotNull
    with(actual!!) {
      if (skills?.isEmpty() == false) {
        failWithMessage("Expected no personal skills, but was ${skills.size}")
      }
    }
    return this
  }

  fun hasNumberOfPersonalSkills(expected: Int): PersonalSkillsAndInterestsResponseAssert {
    isNotNull
    with(actual!!) {
      if ((skills == null || skills.size == 0) && expected == 0) {
        return@with
      }

      if (skills?.size != expected) {
        failWithMessage("Expected number of personal skills to be $expected, but was ${skills?.size ?: "null"}")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [PersonalSkill]. Takes a lambda as the method argument
   * to call assertion methods provided by [PersonalSkillAssert].
   * Returns this [PersonalSkillsAndInterestsResponseAssert] to allow further chained assertions on the parent [PersonalSkillsAndInterestsResponse]
   *
   * The `skillNumber` parameter is not zero indexed to make for better readability in tests. IE. the first skill
   * should be referenced as `.personalSkill(1) { .... }`
   */
  fun personalSkill(skillNumber: Int, consumer: Consumer<PersonalSkillAssert>): PersonalSkillsAndInterestsResponseAssert {
    isNotNull
    with(actual!!) {
      val personalSkill = skills?.get(skillNumber - 1)
      consumer.accept(assertThat(personalSkill))
    }
    return this
  }
}
