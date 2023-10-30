package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import aValidCreatePrisonWorkAndEducationRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidInPrisonInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidInPrisonTrainingInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidInPrisonWorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidPrisonWorkAndEducationResponse
import java.time.OffsetDateTime
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InPrisonTrainingType as InPrisonTrainingTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InPrisonWorkType as InPrisonWorkTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonTrainingType as InPrisonTrainingTypeApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonWorkType as InPrisonWorkTypeApi

@ExtendWith(MockitoExtension::class)
class InPrisonInterestsResourceMapperTest {

  @InjectMocks
  private lateinit var mapper: InPrisonInterestsResourceMapper

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Test
  fun `should map to CreateInPrisonInterestsDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidCreatePrisonWorkAndEducationRequest()
    val expectedInPrisonWorkInterest = listOf(aValidInPrisonWorkInterest())
    val expectedInPrisonTrainingInterest = listOf(aValidInPrisonTrainingInterest())

    // When
    val actual = mapper.toCreateInPrisonInterestsDto(request, prisonId)

    // Then
    assertThat(actual!!.prisonId).isEqualTo(prisonId)
    assertThat(actual.inPrisonWorkInterests).isEqualTo(expectedInPrisonWorkInterest)
    assertThat(actual.inPrisonTrainingInterests).isEqualTo(expectedInPrisonTrainingInterest)
  }

  @Test
  fun `should map to PrisonWorkAndEducationResponse`() {
    // Given
    val inPrisonInterests = aValidInPrisonInterests(
      inPrisonWorkInterests = listOf(
        aValidInPrisonWorkInterest(
          workType = InPrisonWorkTypeDomain.OTHER,
          workTypeOther = "Any in-prison work",
        ),
      ),
      inPrisonTrainingInterests = listOf(
        aValidInPrisonTrainingInterest(
          trainingType = InPrisonTrainingTypeDomain.OTHER,
          trainingTypeOther = "Any in-prison training",
        ),
      ),
    )
    val modifiedDateTime = OffsetDateTime.now()
    given(instantMapper.toOffsetDateTime(any())).willReturn(modifiedDateTime)
    val expectedResponse = aValidPrisonWorkAndEducationResponse(
      id = inPrisonInterests.reference,
      inPrisonWork = setOf(InPrisonWorkTypeApi.OTHER),
      inPrisonWorkOther = "Any in-prison work",
      inPrisonEducation = setOf(InPrisonTrainingTypeApi.OTHER),
      inPrisonEducationOther = "Any in-prison training",
      modifiedDateTime = modifiedDateTime,
    )

    // When
    val actual = mapper.toPrisonWorkAndEducationResponse(inPrisonInterests)

    // Then
    assertThat(actual).isEqualTo(expectedResponse)
  }

  @Test
  fun `should map to PrisonWorkAndEducationResponse given empty collections`() {
    // Given
    val inPrisonInterests = aValidInPrisonInterests(
      inPrisonWorkInterests = emptyList(),
      inPrisonTrainingInterests = emptyList(),
    )
    val modifiedDateTime = OffsetDateTime.now()
    given(instantMapper.toOffsetDateTime(any())).willReturn(modifiedDateTime)
    val expectedResponse = aValidPrisonWorkAndEducationResponse(
      id = inPrisonInterests.reference,
      inPrisonWork = emptySet(),
      inPrisonWorkOther = null,
      inPrisonEducation = emptySet(),
      inPrisonEducationOther = null,
      modifiedDateTime = modifiedDateTime,
    )

    // When
    val actual = mapper.toPrisonWorkAndEducationResponse(inPrisonInterests)

    // Then
    assertThat(actual).isEqualTo(expectedResponse)
  }
}
