package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkInterestType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidFutureWorkInterests
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidWorkInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.UserDetailsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateFutureWorkInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidFutureWorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidFutureWorkInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdateFutureWorkInterestsRequest
import java.time.OffsetDateTime

@ExtendWith(MockitoExtension::class)
class WorkInterestsResourceMapperTest {

  @InjectMocks
  private lateinit var mapper: WorkInterestsResourceMapper

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Mock
  private lateinit var userService: ManageUserService

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
    given(userService.getUserDetails(any())).willReturn(
      UserDetailsDto("asmith_gen", true, "Alex Smith"),
      UserDetailsDto("bjones_gen", true, "Barry Jones"),
    )
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

  @Test
  fun `should map to UpdateFutureWorkInterestsDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidUpdateFutureWorkInterestsRequest()
    val expected = aValidUpdateFutureWorkInterestsDto(
      reference = request.reference!!,
      interests = listOf(
        aValidWorkInterest(
          workType = WorkInterestType.OTHER,
          workTypeOther = "Any job I can get",
          role = "Any role",
        ),
      ),
    )

    // When
    val actual = mapper.toUpdateFutureWorkInterestsDto(request, prisonId)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
