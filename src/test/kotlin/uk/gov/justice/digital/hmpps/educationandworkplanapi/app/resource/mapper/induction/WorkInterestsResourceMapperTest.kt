package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkInterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidWorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateFutureWorkInterestsRequest

class WorkInterestsResourceMapperTest {

  private val mapper = WorkInterestsResourceMapperImpl()

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
}
