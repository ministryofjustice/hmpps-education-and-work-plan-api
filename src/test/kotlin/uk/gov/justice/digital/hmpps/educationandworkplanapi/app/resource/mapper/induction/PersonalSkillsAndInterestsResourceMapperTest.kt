package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.SkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidSkillsAndInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PersonalInterest as PersonalInterestDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PersonalSkill as PersonalSkillDomain

class PersonalSkillsAndInterestsResourceMapperTest {
  private val mapper = PersonalSkillsAndInterestsResourceMapper()

  @Test
  fun `should map to CreatePersonalSkillsAndInterestsDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidSkillsAndInterestsRequest()
    val expectedSkills = listOf(PersonalSkillDomain(SkillType.OTHER, "Hidden skills"))
    val expectedInterests = listOf(PersonalInterestDomain(InterestType.OTHER, "Secret interests"))

    // When
    val actual = mapper.toCreatePersonalSkillsAndInterestsDto(request, prisonId)

    // Then
    assertThat(actual!!.prisonId).isEqualTo(prisonId)
    assertThat(actual.skills).isEqualTo(expectedSkills)
    assertThat(actual.interests).isEqualTo(expectedInterests)
  }
}
