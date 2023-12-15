package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.SkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidPersonalInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidPersonalSkill
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreatePersonalSkillsAndInterestsRequest

class SkillsAndInterestsResourceMapperTest {

  private val mapper = SkillsAndInterestsResourceMapperImpl()

  @Test
  fun `should map to CreatePersonalSkillsAndInterestsDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidCreatePersonalSkillsAndInterestsRequest()
    val expected = aValidCreatePersonalSkillsAndInterestsDto(
      skills = listOf(
        aValidPersonalSkill(
          skillType = SkillType.OTHER,
          skillTypeOther = "Hidden skills",
        ),
      ),
      interests = listOf(
        aValidPersonalInterest(
          interestType = InterestType.OTHER,
          interestTypeOther = "Secret interests",
        ),
      ),
    )

    // When
    val actual = mapper.toCreatePersonalSkillsAndInterestsDto(request, prisonId)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
