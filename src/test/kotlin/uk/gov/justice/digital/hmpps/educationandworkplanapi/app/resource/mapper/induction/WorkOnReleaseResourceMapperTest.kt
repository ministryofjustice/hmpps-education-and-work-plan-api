package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidWorkOnRelease
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.UserDetailsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateWorkOnReleaseRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdateWorkOnReleaseRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidWorkOnReleaseResponseForPrisonerNotLookingToWork
import java.time.OffsetDateTime
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.AffectAbilityToWork as AffectAbilityToWorkDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HopingToWork as HopingToWorkDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AffectAbilityToWork as AffectAbilityToWorkApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork as HopingToWorkApi

@ExtendWith(MockitoExtension::class)
class WorkOnReleaseResourceMapperTest {

  @InjectMocks
  private lateinit var mapper: WorkOnReleaseResourceMapper

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Mock
  private lateinit var userService: ManageUserService

  @Test
  fun `should map to CreateWorkOnReleaseDto`() {
    // Given
    val prisonId = "BXI"
    val createWorkOnReleaseRequest = aValidCreateWorkOnReleaseRequest()
    val expected = aValidCreateWorkOnReleaseDto(
      hopingToWork = HopingToWorkDomain.NO,
      affectAbilityToWork = listOf(AffectAbilityToWorkDomain.OTHER),
      affectAbilityToWorkOther = "Employers aren't interested",
    )

    // When
    val actual = mapper.toCreateWorkOnReleaseDto(createWorkOnReleaseRequest, prisonId)

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should map to WorkOnReleaseResponse`() {
    // Given
    val reference = UUID.randomUUID()
    val domain = aValidWorkOnRelease(
      reference = reference,
      hopingToWork = HopingToWorkDomain.NO,
      affectAbilityToWork = listOf(AffectAbilityToWorkDomain.OTHER),
      affectAbilityToWorkOther = "Employers aren't interested",
    )

    given(userService.getUserDetails(any())).willReturn(
      UserDetailsDto("asmith_gen", true, "Alex Smith"),
      UserDetailsDto("bjones_gen", true, "Barry Jones"),
    )

    val expectedDateTime = OffsetDateTime.now()
    val expected = aValidWorkOnReleaseResponseForPrisonerNotLookingToWork(
      reference = domain.reference,
      hopingToWork = HopingToWorkApi.NO,
      affectAbilityToWork = listOf(AffectAbilityToWorkApi.OTHER),
      affectAbilityToWorkOther = "Employers aren't interested",
      createdAt = expectedDateTime,
      createdBy = "asmith_gen",
      createdByDisplayName = "Alex Smith",
      updatedAt = expectedDateTime,
      updatedBy = "bjones_gen",
      updatedByDisplayName = "Barry Jones",
    )
    given(instantMapper.toOffsetDateTime(any())).willReturn(expectedDateTime)

    // When
    val actual = mapper.toWorkOnReleaseResponse(domain)

    // Then
    assertThat(actual).isEqualTo(expected)
    verify(userService).getUserDetails("asmith_gen")
    verify(userService).getUserDetails("bjones_gen")
  }

  @Test
  fun `should map to UpdateWorkOnReleaseDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidUpdateWorkOnReleaseRequest()
    val expected = aValidUpdateWorkOnReleaseDto(
      reference = request.reference,
      hopingToWork = HopingToWorkDomain.NO,
      affectAbilityToWork = listOf(AffectAbilityToWorkDomain.OTHER),
      affectAbilityToWorkOther = "Employers aren't interested",
    )

    // When
    val actual = mapper.toUpdateWorkOnReleaseDto(request, prisonId)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
