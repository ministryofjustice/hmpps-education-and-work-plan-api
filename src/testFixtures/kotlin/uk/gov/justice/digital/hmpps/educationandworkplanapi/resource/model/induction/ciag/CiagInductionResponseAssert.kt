package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AbilityToWorkFactor
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CiagInductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReasonNotToWork
import java.time.OffsetDateTime
import java.util.UUID

fun assertThat(actual: CiagInductionResponse?) = CiagInductionResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [CiagInductionResponse].
 */
class CiagInductionResponseAssert(actual: CiagInductionResponse?) :
  AbstractObjectAssert<CiagInductionResponseAssert, CiagInductionResponse?>(
    actual,
    CiagInductionResponseAssert::class.java,
  ) {

  fun hasReference(expected: UUID): CiagInductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (reference != expected) {
        failWithMessage("Expected reference to be $expected, but was $reference")
      }
    }
    return this
  }

  fun isForOffenderId(expected: String): CiagInductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (offenderId != expected) {
        failWithMessage("Expected offenderId to be $expected, but was $offenderId")
      }
    }
    return this
  }

  fun hasHopingToGetWork(expected: HopingToWork): CiagInductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (hopingToGetWork != expected) {
        failWithMessage("Expected hopingToGetWork to be $expected, but was $hopingToGetWork")
      }
    }
    return this
  }

  fun hasReasonToNotGetWork(expected: Set<ReasonNotToWork>): CiagInductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (reasonToNotGetWork != expected) {
        failWithMessage("Expected reasonToNotGetWork to be $expected, but was $reasonToNotGetWork")
      }
    }
    return this
  }

  fun hasReasonToNotGetWorkOther(expected: String): CiagInductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (reasonToNotGetWorkOther != expected) {
        failWithMessage("Expected reasonToNotGetWorkOther to be $expected, but was $reasonToNotGetWorkOther")
      }
    }
    return this
  }

  fun hasNoReasonToNotGetWorkOther(): CiagInductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (reasonToNotGetWorkOther != null) {
        failWithMessage("Expected reasonToNotGetWorkOther to be null, but was $reasonToNotGetWorkOther")
      }
    }
    return this
  }

  fun hasAbilityToWork(expected: Set<AbilityToWorkFactor>): CiagInductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (abilityToWork != expected) {
        failWithMessage("Expected abilityToWork to be $expected, but was $abilityToWork")
      }
    }
    return this
  }

  fun hasAbilityToWorkOther(expected: String): CiagInductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (abilityToWorkOther != expected) {
        failWithMessage("Expected abilityToWorkOther to be $expected, but was $abilityToWorkOther")
      }
    }
    return this
  }

  fun hasNoAbilityToWorkOther(): CiagInductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (abilityToWorkOther != null) {
        failWithMessage("Expected abilityToWorkOther to be null, but was $abilityToWorkOther")
      }
    }
    return this
  }

  fun hasPrisonId(expected: String): CiagInductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (prisonId != expected) {
        failWithMessage("Expected prisonId to be $expected, but was $prisonId")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: OffsetDateTime): CiagInductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdDateTime != expected) {
        failWithMessage("Expected createdDateTime to be $expected, but was $createdDateTime")
      }
    }
    return this
  }

  fun wasLastModifiedAt(expected: OffsetDateTime): CiagInductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (modifiedDateTime != expected) {
        failWithMessage("Expected modifiedDateTime to be $expected, but was $modifiedDateTime")
      }
    }
    return this
  }

  fun wasLastModifiedAfter(dateTime: OffsetDateTime): CiagInductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (!modifiedDateTime.isAfter(dateTime)) {
        failWithMessage("Expected modifiedDateTime to be after $dateTime, but was $modifiedDateTime")
      }
    }
    return this
  }

  fun wasCreatedBy(expected: String): CiagInductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun wasModifiedBy(expected: String): CiagInductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (modifiedBy != expected) {
        failWithMessage("Expected modifiedBy to be $expected, but was $modifiedBy")
      }
    }
    return this
  }
}
