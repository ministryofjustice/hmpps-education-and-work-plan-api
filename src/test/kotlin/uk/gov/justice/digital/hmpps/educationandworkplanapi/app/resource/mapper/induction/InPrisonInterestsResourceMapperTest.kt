package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInPrisonInterestsRequest

class InPrisonInterestsResourceMapperTest {

  private val mapper = InPrisonInterestsResourceMapperImpl()

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
}
