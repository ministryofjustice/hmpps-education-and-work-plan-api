package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import aValidPrisonWorkAndEducationRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidInPrisonTrainingInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidInPrisonWorkInterest

class InPrisonInterestsResourceMapperTest {
  private val mapper = InPrisonInterestsResourceMapper()

  @Test
  fun `should map to CreateInPrisonInterestsDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidPrisonWorkAndEducationRequest()
    val expectedInPrisonWorkInterest = listOf(aValidInPrisonWorkInterest())
    val expectedInPrisonTrainingInterest = listOf(aValidInPrisonTrainingInterest())

    // When
    val actual = mapper.toCreateInPrisonInterestsDto(request, prisonId)

    // Then
    assertThat(actual!!.prisonId).isEqualTo(prisonId)
    assertThat(actual.inPrisonWorkInterests).isEqualTo(expectedInPrisonWorkInterest)
    assertThat(actual.inPrisonTrainingInterests).isEqualTo(expectedInPrisonTrainingInterest)
  }
}
