package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.domain.induction.aValidWorkOnRelease
import uk.gov.justice.digital.hmpps.domain.induction.dto.aValidCreateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.domain.induction.dto.aValidUpdateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateWorkOnReleaseRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdateWorkOnReleaseRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidWorkOnReleaseResponseForPrisonerNotLookingToWork
import java.time.OffsetDateTime
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.induction.AffectAbilityToWork as AffectAbilityToWorkDomain
import uk.gov.justice.digital.hmpps.domain.induction.HopingToWork as HopingToWorkDomain
import uk.gov.justice.digital.hmpps.domain.induction.NotHopingToWorkReason as NotHopingToWorkReasonDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AffectAbilityToWork as AffectAbilityToWorkApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork as HopingToWorkApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.NotHopingToWorkReason as NotHopingToWorkReasonApi

@ExtendWith(MockitoExtension::class)
class WorkOnReleaseResourceMapperTest {

  @InjectMocks
  private lateinit var mapper: WorkOnReleaseResourceMapperImpl

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Test
  fun `should map to CreateWorkOnReleaseDto`() {
    // Given
    val prisonId = "BXI"
    val createWorkOnReleaseRequest = aValidCreateWorkOnReleaseRequest()
    val expected = aValidCreateWorkOnReleaseDto(
      hopingToWork = HopingToWorkDomain.NO,
      notHopingToWorkReasons = listOf(NotHopingToWorkReasonDomain.OTHER),
      notHopingToWorkOtherReason = "Long term prison sentence",
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
      notHopingToWorkReasons = listOf(NotHopingToWorkReasonDomain.OTHER),
      notHopingToWorkOtherReason = "Long term prison sentence",
      affectAbilityToWork = listOf(AffectAbilityToWorkDomain.OTHER),
      affectAbilityToWorkOther = "Employers aren't interested",
    )
    val expectedDateTime = OffsetDateTime.now()
    val expected = aValidWorkOnReleaseResponseForPrisonerNotLookingToWork(
      reference = domain.reference,
      hopingToWork = HopingToWorkApi.NO,
      notHopingToWorkReasons = listOf(NotHopingToWorkReasonApi.OTHER),
      notHopingToWorkOtherReason = "Long term prison sentence",
      affectAbilityToWork = listOf(AffectAbilityToWorkApi.OTHER),
      affectAbilityToWorkOther = "Employers aren't interested",
      createdAt = expectedDateTime,
      updatedAt = expectedDateTime,
      updatedBy = "bjones_gen",
      updatedByDisplayName = "Barry Jones",
    )
    given(instantMapper.toOffsetDateTime(any())).willReturn(expectedDateTime)

    // When
    val actual = mapper.toWorkOnReleaseResponse(domain)

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should map to UpdateWorkOnReleaseDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidUpdateWorkOnReleaseRequest()
    val expected = aValidUpdateWorkOnReleaseDto(
      reference = request.reference,
      hopingToWork = HopingToWorkDomain.NO,
      notHopingToWorkReasons = listOf(NotHopingToWorkReasonDomain.OTHER),
      notHopingToWorkOtherReason = "Long term prison sentence",
      affectAbilityToWork = listOf(AffectAbilityToWorkDomain.OTHER),
      affectAbilityToWorkOther = "Employers aren't interested",
    )

    // When
    val actual = mapper.toUpdateWorkOnReleaseDto(request, prisonId)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
