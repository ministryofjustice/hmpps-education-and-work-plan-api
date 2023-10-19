package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPersonalInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPersonalSkillEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPersonalSkillsAndInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreatePersonalSkillsAndInterestsDto

@ExtendWith(MockitoExtension::class)
class PersonalSkillsAndInterestsEntityMapperTest {

  @InjectMocks
  private lateinit var mapper: PersonalSkillsAndInterestsEntityMapperImpl

  @Mock
  private lateinit var personalSkillEntityMapper: PersonalSkillEntityMapper

  @Mock
  private lateinit var personalInterestEntityMapper: PersonalInterestEntityMapper

  @Test
  fun `should map from dto to entity`() {
    // Given
    val createPersonalSkillsAndInterestsDto = aValidCreatePersonalSkillsAndInterestsDto()
    val expectedPersonalSkill = aValidPersonalSkillEntity()
    val expectedPersonalInterest = aValidPersonalInterestEntity()
    val expected = aValidPersonalSkillsAndInterestsEntity(
      skills = listOf(expectedPersonalSkill),
      interests = listOf(expectedPersonalInterest),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )
    given(personalSkillEntityMapper.fromDomainToEntity(any())).willReturn(expectedPersonalSkill)
    given(personalInterestEntityMapper.fromDomainToEntity(any())).willReturn(expectedPersonalInterest)

    // When
    val actual = mapper.fromDtoToEntity(createPersonalSkillsAndInterestsDto)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .hasAReference()
      .usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(".*reference")
      .isEqualTo(expected)
    verify(personalSkillEntityMapper).fromDomainToEntity(createPersonalSkillsAndInterestsDto.skills[0])
    verify(personalInterestEntityMapper).fromDomainToEntity(createPersonalSkillsAndInterestsDto.interests[0])
  }
}
