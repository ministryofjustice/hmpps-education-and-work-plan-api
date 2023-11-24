package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.SkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidPersonalInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidPersonalSkill
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidPersonalSkillsAndInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkill
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateSkillsAndInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidSkillsAndInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdateSkillsAndInterestsRequest
import java.time.OffsetDateTime
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PersonalInterest as PersonalInterestDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PersonalSkill as PersonalSkillDomain

@ExtendWith(MockitoExtension::class)
class PersonalSkillsAndInterestsResourceMapperTest {

  @InjectMocks
  private lateinit var mapper: PersonalSkillsAndInterestsResourceMapper

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Test
  fun `should map to CreatePersonalSkillsAndInterestsDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidCreateSkillsAndInterestsRequest()
    val expectedSkills = listOf(PersonalSkillDomain(SkillType.OTHER, "Hidden skills"))
    val expectedInterests = listOf(PersonalInterestDomain(InterestType.OTHER, "Secret interests"))

    // When
    val actual = mapper.toCreatePersonalSkillsAndInterestsDto(request, prisonId)

    // Then
    assertThat(actual!!.prisonId).isEqualTo(prisonId)
    assertThat(actual.skills).isEqualTo(expectedSkills)
    assertThat(actual.interests).isEqualTo(expectedInterests)
  }

  @Test
  fun `should map to SkillsAndInterestsResponse`() {
    // Given
    val skillsAndInterests = aValidPersonalSkillsAndInterests(
      skills = listOf(
        aValidPersonalSkill(
          skillType = SkillType.OTHER,
          skillTypeOther = "Hidden skills",
        ),
      ),
      interests = listOf(
        aValidPersonalInterest(
          interestType = InterestType.OTHER,
          interestTypeOther = "Varied interests",
        ),
      ),
    )
    val modifiedDateTime = OffsetDateTime.now()
    given(instantMapper.toOffsetDateTime(any())).willReturn(modifiedDateTime)
    val expectedResponse = aValidSkillsAndInterestsResponse(
      id = skillsAndInterests.reference,
      skills = setOf(PersonalSkill.OTHER),
      skillsOther = "Hidden skills",
      personalInterests = setOf(PersonalInterest.OTHER),
      personalInterestsOther = "Varied interests",
      modifiedBy = skillsAndInterests.lastUpdatedBy!!,
      modifiedDateTime = modifiedDateTime,
    )

    // When
    val actual = mapper.toSkillsAndInterestsResponse(skillsAndInterests)

    // Then
    assertThat(actual).isEqualTo(expectedResponse)
  }

  @Test
  fun `should map to SkillsAndInterestsResponse given empty collections`() {
    // Given
    val skillsAndInterests = aValidPersonalSkillsAndInterests(
      skills = emptyList(),
      interests = emptyList(),
    )
    val modifiedDateTime = OffsetDateTime.now()
    given(instantMapper.toOffsetDateTime(any())).willReturn(modifiedDateTime)
    val expectedResponse = aValidSkillsAndInterestsResponse(
      id = skillsAndInterests.reference,
      skills = emptySet(),
      skillsOther = null,
      personalInterests = emptySet(),
      personalInterestsOther = null,
      modifiedBy = skillsAndInterests.lastUpdatedBy!!,
      modifiedDateTime = modifiedDateTime,
    )

    // When
    val actual = mapper.toSkillsAndInterestsResponse(skillsAndInterests)

    // Then
    assertThat(actual).isEqualTo(expectedResponse)
  }

  @Test
  fun `should map to UpdatePersonalSkillsAndInterestsDto`() {
    // Given
    val prisonId = "BXI"
    val request = aValidUpdateSkillsAndInterestsRequest()
    val expectedSkills = listOf(PersonalSkillDomain(SkillType.OTHER, "Hidden skills"))
    val expectedInterests = listOf(PersonalInterestDomain(InterestType.OTHER, "Secret interests"))

    // When
    val actual = mapper.toUpdatePersonalSkillsAndInterestsDto(request, prisonId)

    // Then
    assertThat(actual!!.prisonId).isEqualTo(prisonId)
    assertThat(actual.reference).isEqualTo(request.id)
    assertThat(actual.skills).isEqualTo(expectedSkills)
    assertThat(actual.interests).isEqualTo(expectedInterests)
  }
}
