package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.PersonalInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.PersonalSkill
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidPersonalInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidPersonalSkill
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidPersonalSkillsAndInterests
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PersonalInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PersonalSkillEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPersonalInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPersonalSkillEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPersonalSkillsAndInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPersonalSkillsAndInterestsEntityWithJpaFieldsPopulated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat

@ExtendWith(MockitoExtension::class)
class PersonalSkillsAndInterestsEntityMapperTest {

  @InjectMocks
  private lateinit var mapper: PersonalSkillsAndInterestsEntityMapper

  @Mock
  private lateinit var personalSkillEntityMapper: PersonalSkillEntityMapper

  @Mock
  private lateinit var personalInterestEntityMapper: PersonalInterestEntityMapper

  @Mock
  private lateinit var personalSkillEntityListManager: InductionEntityListManager<PersonalSkillEntity, PersonalSkill>

  @Mock
  private lateinit var personalInterestEntityListManager: InductionEntityListManager<PersonalInterestEntity, PersonalInterest>

  @Test
  fun `should map from dto to entity`() {
    // Given
    val createPersonalSkillsAndInterestsDto = aValidCreatePersonalSkillsAndInterestsDto()
    val expectedPersonalSkill = aValidPersonalSkillEntity()
    val expectedPersonalInterest = aValidPersonalInterestEntity()
    val expected = aValidPersonalSkillsAndInterestsEntity(
      skills = mutableListOf(expectedPersonalSkill),
      interests = mutableListOf(expectedPersonalInterest),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )
    given(personalSkillEntityMapper.fromDomainToEntity(any())).willReturn(expectedPersonalSkill)
    given(personalInterestEntityMapper.fromDomainToEntity(any())).willReturn(expectedPersonalInterest)

    // When
    val actual = mapper.fromCreateDtoToEntity(createPersonalSkillsAndInterestsDto)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(".*reference")
      .isEqualTo(expected)
    verify(personalSkillEntityMapper).fromDomainToEntity(createPersonalSkillsAndInterestsDto.skills[0])
    verify(personalInterestEntityMapper).fromDomainToEntity(createPersonalSkillsAndInterestsDto.interests[0])
  }

  @Test
  fun `should map from entity to domain`() {
    // Given
    val personalSkillsAndInterestsEntity = aValidPersonalSkillsAndInterestsEntityWithJpaFieldsPopulated()
    val expectedSkill = aValidPersonalSkill()
    val expectedInterest = aValidPersonalInterest()
    val expectedSkillsAndInterests = aValidPersonalSkillsAndInterests(
      reference = personalSkillsAndInterestsEntity.reference,
      skills = mutableListOf(expectedSkill),
      interests = mutableListOf(expectedInterest),
      createdAt = personalSkillsAndInterestsEntity.createdAt!!,
      createdAtPrison = personalSkillsAndInterestsEntity.createdAtPrison,
      createdBy = personalSkillsAndInterestsEntity.createdBy!!,
      lastUpdatedAt = personalSkillsAndInterestsEntity.updatedAt!!,
      lastUpdatedAtPrison = personalSkillsAndInterestsEntity.updatedAtPrison,
      lastUpdatedBy = personalSkillsAndInterestsEntity.updatedBy!!,
    )
    given(personalSkillEntityMapper.fromEntityToDomain(any())).willReturn(expectedSkill)
    given(personalInterestEntityMapper.fromEntityToDomain(any())).willReturn(expectedInterest)

    // When
    val actual = mapper.fromEntityToDomain(personalSkillsAndInterestsEntity)

    // Then
    assertThat(actual).isEqualTo(expectedSkillsAndInterests)
    verify(personalSkillEntityMapper).fromEntityToDomain(personalSkillsAndInterestsEntity.skills[0])
    verify(personalInterestEntityMapper).fromEntityToDomain(personalSkillsAndInterestsEntity.interests[0])
  }
}
