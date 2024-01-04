package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkInterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidFutureWorkInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidWorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateFutureWorkInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidFutureWorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidFutureWorkInterestsResponse
import java.time.OffsetDateTime

@ExtendWith(MockitoExtension::class)
class WorkInterestsResourceMapperTest {

  @InjectMocks
  private lateinit var mapper: WorkInterestsResourceMapperImpl

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Test
  fun `should map to CreateFutureWorkInterestsDto`() {
    // Given
    val prisonId = "BXI"
    val createFutureWorkInterestsRequest = aValidCreateFutureWorkInterestsRequest()
    val expected = aValidCreateFutureWorkInterestsDto(
      interests = listOf(
        aValidWorkInterest(
          workType = WorkInterestType.OTHER,
          workTypeOther = "Any job I can get",
          role = "Any role",
        ),
      ),
    )

    // When
    val actual = mapper.toCreateFutureWorkInterestsDto(createFutureWorkInterestsRequest, prisonId)

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should map to FutureWorkInterestsResponse`() {
    // Given
    val domain = aValidFutureWorkInterests()
    val expectedDateTime = OffsetDateTime.now()
    val expected = aValidFutureWorkInterestsResponse(
      reference = domain.reference,
      interests = listOf(
        aValidFutureWorkInterest(
          workType = WorkType.CONSTRUCTION,
          workTypeOther = null,
          role = "Bricklaying",
        ),
      ),
      createdAt = expectedDateTime,
      updatedAt = expectedDateTime,
      updatedBy = "bjones_gen",
      updatedByDisplayName = "Barry Jones",
    )
    given(instantMapper.toOffsetDateTime(any())).willReturn(expectedDateTime)

    // When
    val actual = mapper.toFutureWorkInterestsResponse(domain)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
