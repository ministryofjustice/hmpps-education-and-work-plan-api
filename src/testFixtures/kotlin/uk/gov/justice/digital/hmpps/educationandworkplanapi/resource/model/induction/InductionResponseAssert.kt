package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.isBeforeRounded
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionResponse
import java.time.OffsetDateTime
import java.util.UUID
import java.util.function.Consumer

fun assertThat(actual: InductionResponse?) = InductionResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [InductionResponse].
 */
class InductionResponseAssert(actual: InductionResponse?) :
  AbstractObjectAssert<InductionResponseAssert, InductionResponse?>(
    actual,
    InductionResponseAssert::class.java,
  ) {

  fun hasReference(expected: UUID): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (reference != expected) {
        failWithMessage("Expected reference to be $expected, but was $reference")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: OffsetDateTime): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedAtOrAfter(dateTime: OffsetDateTime): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAt.isBeforeRounded(dateTime)) {
        failWithMessage("Expected createdAt to be after $dateTime, but was $createdAt")
      }
    }
    return this
  }

  fun wasUpdatedAt(expected: OffsetDateTime): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt != expected) {
        failWithMessage("Expected updatedAt to be $expected, but was $updatedAt")
      }
    }
    return this
  }

  fun wasUpdatedAtOrAfter(dateTime: OffsetDateTime): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt.isBeforeRounded(dateTime)) {
        failWithMessage("Expected updatedAt to be after $dateTime, but was $updatedAt")
      }
    }
    return this
  }

  fun wasCreatedBy(expected: String): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun wasCreatedByDisplayName(expected: String): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdByDisplayName != expected) {
        failWithMessage("Expected createdByDisplayName to be $expected, but was $createdByDisplayName")
      }
    }
    return this
  }

  fun wasUpdatedBy(expected: String): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedBy != expected) {
        failWithMessage("Expected updatedBy to be $expected, but was $updatedBy")
      }
    }
    return this
  }

  fun wasUpdatedByDisplayName(expected: String): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedByDisplayName != expected) {
        failWithMessage("Expected updatedByDisplayName to be $expected, but was $updatedByDisplayName")
      }
    }
    return this
  }

  fun wasCreatedAtPrison(expected: String): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAtPrison != expected) {
        failWithMessage("Expected createdAtPrison to be $expected, but was $createdAtPrison")
      }
    }
    return this
  }

  fun wasUpdatedAtPrison(expected: String): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAtPrison != expected) {
        failWithMessage("Expected updatedAtPrison to be $expected, but was $updatedAtPrison")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the child [PreviousWorkExperiencesResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [PreviousWorkExperiencesResponseAssert].
   * Returns this [InductionResponseAssert] to allow further chained assertions on the parent [InductionResponse]
   */
  fun previousWorkExperiences(consumer: Consumer<PreviousWorkExperiencesResponseAssert>): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      consumer.accept(assertThat(previousWorkExperiences))
    }
    return this
  }

  /**
   * Allows for assertion chaining into the child [FutureWorkInterestsResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [FutureWorkInterestsResponseAssert].
   * Returns this [InductionResponseAssert] to allow further chained assertions on the parent [InductionResponse]
   */
  fun futureWorkInterests(consumer: Consumer<FutureWorkInterestsResponseAssert>): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      consumer.accept(assertThat(futureWorkInterests))
    }
    return this
  }

  /**
   * Allows for assertion chaining into the child [WorkOnReleaseResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [WorkOnReleaseResponseAssert].
   * Returns this [InductionResponseAssert] to allow further chained assertions on the parent [InductionResponse]
   */
  fun workOnRelease(consumer: Consumer<WorkOnReleaseResponseAssert>): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      consumer.accept(assertThat(workOnRelease))
    }
    return this
  }

  /**
   * Allows for assertion chaining into the child [PreviousTrainingResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [PreviousTrainingResponseAssert].
   * Returns this [InductionResponseAssert] to allow further chained assertions on the parent [InductionResponse]
   */
  fun previousTraining(consumer: Consumer<PreviousTrainingResponseAssert>): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      consumer.accept(assertThat(previousTraining))
    }
    return this
  }

  /**
   * Allows for assertion chaining into the child [PersonalSkillsAndInterestsResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [PersonalSkillsAndInterestsResponseAssert].
   * Returns this [InductionResponseAssert] to allow further chained assertions on the parent [InductionResponse]
   */
  fun personalSkillsAndInterests(consumer: Consumer<PersonalSkillsAndInterestsResponseAssert>): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      consumer.accept(assertThat(personalSkillsAndInterests))
    }
    return this
  }

  /**
   * Allows for assertion chaining into the child [PreviousQualificationsResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [PreviousQualificationsResponseAssert].
   * Returns this [InductionResponseAssert] to allow further chained assertions on the parent [InductionResponse]
   */
  fun previousQualifications(consumer: Consumer<PreviousQualificationsResponseAssert>): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      consumer.accept(assertThat(previousQualifications))
    }
    return this
  }

  /**
   * Allows for assertion chaining into the child [InPrisonInterestsResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [InPrisonInterestsResponseAssert].
   * Returns this [InductionResponseAssert] to allow further chained assertions on the parent [InductionResponse]
   */
  fun inPrisonInterests(consumer: Consumer<InPrisonInterestsResponseAssert>): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      consumer.accept(assertThat(inPrisonInterests))
    }
    return this
  }
}
