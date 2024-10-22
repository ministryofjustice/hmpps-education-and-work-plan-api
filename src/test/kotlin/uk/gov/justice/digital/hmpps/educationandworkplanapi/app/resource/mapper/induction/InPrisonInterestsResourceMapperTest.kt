package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInPrisonInterests
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.UserDetailsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInPrisonInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidInPrisonInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdateInPrisonInterestsRequest
import java.time.OffsetDateTime

@ExtendWith(MockitoExtension::class)
class InPrisonInterestsResourceMapperTest {

  @InjectMocks
  private lateinit var mapper: InPrisonInterestsResourceMapper

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Mock
  private lateinit var userService: ManageUserService

  @Test
  fun `should map to CreateInPrisonInterestsDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidCreateInPrisonInterestsRequest()
    val expected = aValidCreateInPrisonInterestsDto()

    // When
    val actual = mapper.toCreateInPrisonInterestsDto(request, prisonId)

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should map to InPrisonInterestsResponse`() {
    // Given

    given(userService.getUserDetails(any())).willReturn(
      UserDetailsDto("asmith_gen", true, "Alex Smith"),
      UserDetailsDto("bjones_gen", true, "Barry Jones"),
    )
    val domain = aValidInPrisonInterests()
    val expectedDateTime = OffsetDateTime.now()
    val expected = aValidInPrisonInterestsResponse(
      reference = domain.reference,
      createdAt = expectedDateTime,
      updatedAt = expectedDateTime,
      updatedBy = "bjones_gen",
      updatedByDisplayName = "Barry Jones",
    )
    given(instantMapper.toOffsetDateTime(any())).willReturn(expectedDateTime)

    // When
    val actual = mapper.toInPrisonInterestsResponse(domain)

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should map to UpdateInPrisonInterestsDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidUpdateInPrisonInterestsRequest()
    val expected = aValidUpdateInPrisonInterestsDto(reference = request.reference!!)

    // When
    val actual = mapper.toUpdateInPrisonInterestsDto(request, prisonId)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
