package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkInterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkInterestDetail
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateWorkInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdateWorkInterestsRequest

class FutureWorkInterestsResourceMapperTest {
  private val mapper = FutureWorkInterestsResourceMapper()

  @Test
  fun `should map to CreateFutureWorkInterestsDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidCreateWorkInterestsRequest(
      workInterests = setOf(WorkType.OTHER, WorkType.TECHNICAL),
      workInterestsOther = "Entertainment",
      particularJobInterests = setOf(
        WorkInterestDetail(
          workInterest = WorkType.OTHER,
          role = "Juggler",
        ),
        WorkInterestDetail(
          workInterest = WorkType.TECHNICAL,
          role = "Kotlin Developer",
        ),
      )
    )
    val expectedInterests = listOf(
      WorkInterest(
        workType = WorkInterestType.OTHER,
        workTypeOther = "Entertainment",
        role = "Juggler",
      ),
      WorkInterest(
        workType = WorkInterestType.TECHNICAL,
        workTypeOther = null,
        role = "Kotlin Developer",
      ),
    )

    // When
    val actual = mapper.toCreateFutureWorkInterestsDto(request, prisonId)

    // Then
    assertThat(actual!!.prisonId).isEqualTo(prisonId)
    assertThat(actual.interests).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(expectedInterests)
  }

  @Test
  fun `should map to UpdateFutureWorkInterestsDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidUpdateWorkInterestsRequest(
      workInterests = setOf(WorkType.OTHER, WorkType.TECHNICAL),
      workInterestsOther = "Entertainment",
      particularJobInterests = setOf(
        WorkInterestDetail(
          workInterest = WorkType.OTHER,
          role = "Juggler",
        ),
        WorkInterestDetail(
          workInterest = WorkType.TECHNICAL,
          role = "Kotlin Developer",
        ),
      )
    )
    val expectedInterests = listOf(
      WorkInterest(
        workType = WorkInterestType.OTHER,
        workTypeOther = "Entertainment",
        role = "Juggler",
      ),
      WorkInterest(
        workType = WorkInterestType.TECHNICAL,
        workTypeOther = null,
        role = "Kotlin Developer",
      ),
    )

    // When
    val actual = mapper.toUpdateFutureWorkInterestsDto(request, prisonId)

    // Then
    assertThat(actual!!.prisonId).isEqualTo(prisonId)
    assertThat(actual.reference).isEqualTo(request.id)
    assertThat(actual.interests).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(expectedInterests)
  }

  @Test
  fun `should map to UpdateFutureWorkInterestsDto with missing particular job interest`() {
    // Given
    val prisonId = "BXI"
    val request = aValidUpdateWorkInterestsRequest(
      workInterests = setOf(WorkType.OTHER, WorkType.TECHNICAL),
      workInterestsOther = "Entertainment",
      particularJobInterests = setOf(
        WorkInterestDetail(
          workInterest = WorkType.OTHER,
          role = "Juggler",
        ),
      )
    )
    val expectedInterests = listOf(
      WorkInterest(
        workType = WorkInterestType.OTHER,
        workTypeOther = "Entertainment",
        role = "Juggler",
      ),
      WorkInterest(
        workType = WorkInterestType.TECHNICAL,
        workTypeOther = null,
        role = null,
      ),
    )

    // When
    val actual = mapper.toUpdateFutureWorkInterestsDto(request, prisonId)

    // Then
    assertThat(actual!!.prisonId).isEqualTo(prisonId)
    assertThat(actual.reference).isEqualTo(request.id)
    assertThat(actual.interests).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(expectedInterests)
  }

  @Test
  fun `should map to UpdateFutureWorkInterestsDto when particular job interests is null`() {
    // Given
    val prisonId = "BXI"
    val request = aValidUpdateWorkInterestsRequest(
      workInterests = setOf(WorkType.TECHNICAL),
      workInterestsOther = "",
      particularJobInterests = emptySet(),
    )
    val expectedInterests = listOf(
      WorkInterest(
        workType = WorkInterestType.TECHNICAL,
        workTypeOther = null,
        role = null,
      ),
    )

    // When
    val actual = mapper.toUpdateFutureWorkInterestsDto(request, prisonId)

    // Then
    assertThat(actual!!.prisonId).isEqualTo(prisonId)
    assertThat(actual.reference).isEqualTo(request.id)
    assertThat(actual.interests).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(expectedInterests)
  }
}
