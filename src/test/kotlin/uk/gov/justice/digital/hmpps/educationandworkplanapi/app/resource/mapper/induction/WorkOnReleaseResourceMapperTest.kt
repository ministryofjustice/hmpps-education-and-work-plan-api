package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateWorkOnReleaseRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.AffectAbilityToWork as AffectAbilityToWorkDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.HopingToWork as HopingToWorkDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.NotHopingToWorkReason as NotHopingToWorkReasonDomain

class WorkOnReleaseResourceMapperTest {

  private val mapper = WorkOnReleaseResourceMapperImpl()

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
}
