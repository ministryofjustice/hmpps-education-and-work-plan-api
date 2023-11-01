package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkInterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateWorkInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidWorkInterests

class FutureWorkInterestsResourceMapperTest {
  private val mapper = FutureWorkInterestsResourceMapper()

  @Test
  fun `should map to CreateFutureWorkInterestsDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidCreateWorkInterestsRequest()

    // When
    val actual = mapper.toCreateFutureWorkInterestsDto(request, prisonId)

    // Then
    assertThat(actual!!.prisonId).isEqualTo(prisonId)
    assertThat(actual.interests).hasSize(1)
    assertThat(actual.interests[0].workType).isEqualTo(WorkInterestType.OTHER)
    assertThat(actual.interests[0].workTypeOther).isEqualTo("Any job I can get")
    assertThat(actual.interests[0].role).isEqualTo("Any role")
  }

  @Test
  fun `should map to UpdateFutureWorkInterestsDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidWorkInterests()

    // When
    val actual = mapper.toUpdateFutureWorkInterestsDto(request, prisonId)

    // Then
    assertThat(actual!!.prisonId).isEqualTo(prisonId)
    assertThat(actual.reference).isEqualTo(request.id)
    assertThat(actual.interests).hasSize(1)
    assertThat(actual.interests[0].workType).isEqualTo(WorkInterestType.OTHER)
    assertThat(actual.interests[0].workTypeOther).isEqualTo("Any job I can get")
    assertThat(actual.interests[0].role).isEqualTo("Any role")
  }
}
