package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPersonalSkillsAndInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousQualificationsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousTrainingEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousWorkExperiencesEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidWorkOnReleaseEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
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
    given(futureWorkInterestsEntityMapper.fromDtoToEntity(any())).willReturn(expectedFutureWorkInterestsEntity)
    given(inPrisonInterestsEntityMapper.fromDtoToEntity(any())).willReturn(expectedInPrisonInterestsEntity)
    given(personalSkillsAndInterestsEntityMapper.fromDtoToEntity(any())).willReturn(expectedPersonalSkillsAndInterestsEntity)
    given(previousQualificationsEntityMapper.fromDtoToEntity(any())).willReturn(expectedPreviousQualificationsEntity)
    given(previousTrainingEntityMapper.fromDtoToEntity(any())).willReturn(expectedPreviousTrainingEntity)
    given(previousWorkExperiencesEntityMapper.fromDtoToEntity(any())).willReturn(expectedPreviousWorkExperiencesEntity)
    given(workOnReleaseEntityMapper.fromDtoToEntity(any())).willReturn(expectedWorkOnReleaseEntity)

    // When
    val actual = mapper.fromDtoToEntity(createInductionDto)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .hasAReference()
      .usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(".*reference")
      .isEqualTo(expected)
    verify(futureWorkInterestsEntityMapper).fromDtoToEntity(createInductionDto.futureWorkInterests!!)
    verify(inPrisonInterestsEntityMapper).fromDtoToEntity(createInductionDto.inPrisonInterests!!)
    verify(personalSkillsAndInterestsEntityMapper).fromDtoToEntity(createInductionDto.personalSkillsAndInterests!!)
    verify(previousQualificationsEntityMapper).fromDtoToEntity(createInductionDto.previousQualifications!!)
    verify(previousTrainingEntityMapper).fromDtoToEntity(createInductionDto.previousTraining!!)
    verify(previousWorkExperiencesEntityMapper).fromDtoToEntity(createInductionDto.previousWorkExperiences!!)
    verify(workOnReleaseEntityMapper).fromDtoToEntity(createInductionDto.workOnRelease)
  }
}
