package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.AffectAbilityToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.HopingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.NotHopingToWorkReason
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateCiagInductionRequest

class WorkOnReleaseResourceMapperTest {
  private val mapper = WorkOnReleaseResourceMapperImpl()

  @Test
  fun `should map to PreviousTrainingDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidCreateCiagInductionRequest(prisonId = prisonId)
    val expectedNotHopingToWorkReasons = listOf(NotHopingToWorkReason.OTHER)
    val expectedNotHopingToWorkOtherReason = "Crime pays"
    val expectedAffectAbilityToWork = listOf(AffectAbilityToWork.OTHER)
    val expectedAbilityToWorkOther = "Lack of interest"

    // When
    val actual = mapper.toCreateWorkOnReleaseDto(request)

    // Then
    assertThat(actual.prisonId).isEqualTo(prisonId)
    assertThat(actual.hopingToWork).isEqualTo(HopingToWork.NOT_SURE)
    assertThat(actual.notHopingToWorkReasons).isEqualTo(expectedNotHopingToWorkReasons)
    assertThat(actual.notHopingToWorkOtherReason).isEqualTo(expectedNotHopingToWorkOtherReason)
    assertThat(actual.affectAbilityToWork).isEqualTo(expectedAffectAbilityToWork)
    assertThat(actual.affectAbilityToWorkOther).isEqualTo(expectedAbilityToWorkOther)
  }
}
