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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidFutureWorkInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInPrisonInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInductionEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInductionEntityWithJpaFieldsPopulated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPersonalSkillsAndInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousQualificationsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousTrainingEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousWorkExperiencesEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidWorkOnReleaseEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidFutureWorkInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidInPrisonInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidInduction
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidPersonalSkillsAndInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidPreviousQualifications
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidPreviousTraining
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidPreviousWorkExperiences
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidWorkOnRelease
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreateInductionDto

@ExtendWith(MockitoExtension::class)
class InductionEntityMapperTest {

  @InjectMocks
  private lateinit var mapper: InductionEntityMapperImpl

  @Mock
  private lateinit var futureWorkInterestsEntityMapper: FutureWorkInterestsEntityMapper

  @Mock
  private lateinit var inPrisonInterestsEntityMapper: InPrisonInterestsEntityMapper

  @Mock
  private lateinit var personalSkillsAndInterestsEntityMapper: PersonalSkillsAndInterestsEntityMapper

  @Mock
  private lateinit var previousQualificationsEntityMapper: PreviousQualificationsEntityMapper

  @Mock
  private lateinit var previousTrainingEntityMapper: PreviousTrainingEntityMapper

  @Mock
  private lateinit var previousWorkExperiencesEntityMapper: PreviousWorkExperiencesEntityMapper

  @Mock
  private lateinit var workOnReleaseEntityMapper: WorkOnReleaseEntityMapper

  @Test
  fun `should map from dto to entity`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val prisonId = "BXI"
    val createInductionDto = aValidCreateInductionDto(
      prisonNumber = prisonNumber,
      prisonId = prisonId,
    )
    val expectedFutureWorkInterestsEntity = aValidFutureWorkInterestsEntity()
    val expectedInPrisonInterestsEntity = aValidInPrisonInterestsEntity()
    val expectedPersonalSkillsAndInterestsEntity = aValidPersonalSkillsAndInterestsEntity()
    val expectedPreviousQualificationsEntity = aValidPreviousQualificationsEntity()
    val expectedPreviousTrainingEntity = aValidPreviousTrainingEntity()
    val expectedPreviousWorkExperiencesEntity = aValidPreviousWorkExperiencesEntity()
    val expectedWorkOnReleaseEntity = aValidWorkOnReleaseEntity()
    val expected = aValidInductionEntity(
      prisonNumber = prisonNumber,
      workOnRelease = expectedWorkOnReleaseEntity,
      previousQualifications = expectedPreviousQualificationsEntity,
      previousTraining = expectedPreviousTrainingEntity,
      previousWorkExperiences = expectedPreviousWorkExperiencesEntity,
      inPrisonInterests = expectedInPrisonInterestsEntity,
      personalSkillsAndInterests = expectedPersonalSkillsAndInterestsEntity,
      futureWorkInterests = expectedFutureWorkInterestsEntity,
      createdAtPrison = prisonId,
      updatedAtPrison = prisonId,
    )
    given(futureWorkInterestsEntityMapper.fromCreateDtoToEntity(any())).willReturn(expectedFutureWorkInterestsEntity)
    given(inPrisonInterestsEntityMapper.fromCreateDtoToEntity(any())).willReturn(expectedInPrisonInterestsEntity)
    given(personalSkillsAndInterestsEntityMapper.fromCreateDtoToEntity(any())).willReturn(expectedPersonalSkillsAndInterestsEntity)
    given(previousQualificationsEntityMapper.fromCreateDtoToEntity(any())).willReturn(expectedPreviousQualificationsEntity)
    given(previousTrainingEntityMapper.fromCreateDtoToEntity(any())).willReturn(expectedPreviousTrainingEntity)
    given(previousWorkExperiencesEntityMapper.fromCreateDtoToEntity(any())).willReturn(expectedPreviousWorkExperiencesEntity)
    given(workOnReleaseEntityMapper.fromCreateDtoToEntity(any())).willReturn(expectedWorkOnReleaseEntity)

    // When
    val actual = mapper.fromCreateDtoToEntity(createInductionDto)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .hasAReference()
      .usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(".*reference")
      .isEqualTo(expected)
    verify(futureWorkInterestsEntityMapper).fromCreateDtoToEntity(createInductionDto.futureWorkInterests!!)
    verify(inPrisonInterestsEntityMapper).fromCreateDtoToEntity(createInductionDto.inPrisonInterests!!)
    verify(personalSkillsAndInterestsEntityMapper).fromCreateDtoToEntity(createInductionDto.personalSkillsAndInterests!!)
    verify(previousQualificationsEntityMapper).fromCreateDtoToEntity(createInductionDto.previousQualifications!!)
    verify(previousTrainingEntityMapper).fromCreateDtoToEntity(createInductionDto.previousTraining!!)
    verify(previousWorkExperiencesEntityMapper).fromCreateDtoToEntity(createInductionDto.previousWorkExperiences!!)
    verify(workOnReleaseEntityMapper).fromCreateDtoToEntity(createInductionDto.workOnRelease)
  }

  @Test
  fun `should map from entity to domain`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val inductionEntity = aValidInductionEntityWithJpaFieldsPopulated(prisonNumber = prisonNumber)
    val expectedInduction = aValidInduction(
      reference = inductionEntity.reference!!,
      prisonNumber = prisonNumber,
      workOnRelease = aValidWorkOnRelease(),
      previousQualifications = aValidPreviousQualifications(),
      previousTraining = aValidPreviousTraining(),
      previousWorkExperiences = aValidPreviousWorkExperiences(),
      inPrisonInterests = aValidInPrisonInterests(),
      personalSkillsAndInterests = aValidPersonalSkillsAndInterests(),
      futureWorkInterests = aValidFutureWorkInterests(),
      createdAt = inductionEntity.createdAt!!,
      createdAtPrison = inductionEntity.createdAtPrison!!,
      createdBy = inductionEntity.createdBy!!,
      createdByDisplayName = inductionEntity.createdByDisplayName!!,
      lastUpdatedAt = inductionEntity.updatedAt!!,
      lastUpdatedAtPrison = inductionEntity.updatedAtPrison!!,
      lastUpdatedBy = inductionEntity.updatedBy!!,
      lastUpdatedByDisplayName = inductionEntity.updatedByDisplayName!!,
    )
    given(futureWorkInterestsEntityMapper.fromEntityToDomain(any())).willReturn(expectedInduction.futureWorkInterests)
    given(inPrisonInterestsEntityMapper.fromEntityToDomain(any())).willReturn(expectedInduction.inPrisonInterests)
    given(personalSkillsAndInterestsEntityMapper.fromEntityToDomain(any())).willReturn(expectedInduction.personalSkillsAndInterests)
    given(previousQualificationsEntityMapper.fromEntityToDomain(any())).willReturn(expectedInduction.previousQualifications)
    given(previousTrainingEntityMapper.fromEntityToDomain(any())).willReturn(expectedInduction.previousTraining)
    given(previousWorkExperiencesEntityMapper.fromEntityToDomain(any())).willReturn(expectedInduction.previousWorkExperiences)
    given(workOnReleaseEntityMapper.fromEntityToDomain(any())).willReturn(expectedInduction.workOnRelease)

    // When
    val actual = mapper.fromEntityToDomain(inductionEntity)

    // Then
    assertThat(actual).isEqualTo(expectedInduction)
    verify(futureWorkInterestsEntityMapper).fromEntityToDomain(inductionEntity.futureWorkInterests!!)
    verify(inPrisonInterestsEntityMapper).fromEntityToDomain(inductionEntity.inPrisonInterests!!)
    verify(personalSkillsAndInterestsEntityMapper).fromEntityToDomain(inductionEntity.personalSkillsAndInterests!!)
    verify(previousQualificationsEntityMapper).fromEntityToDomain(inductionEntity.previousQualifications!!)
    verify(previousTrainingEntityMapper).fromEntityToDomain(inductionEntity.previousTraining!!)
    verify(previousWorkExperiencesEntityMapper).fromEntityToDomain(inductionEntity.previousWorkExperiences!!)
    verify(workOnReleaseEntityMapper).fromEntityToDomain(inductionEntity.workOnRelease!!)
  }
}
