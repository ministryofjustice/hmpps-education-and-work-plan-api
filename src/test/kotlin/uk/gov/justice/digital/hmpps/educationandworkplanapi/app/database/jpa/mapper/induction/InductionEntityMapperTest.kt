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
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.aValidPreviousQualifications
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aFullyPopulatedInduction
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidFutureWorkInterests
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInPrisonInterests
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInductionSummary
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidPersonalSkillsAndInterests
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidPreviousTraining
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidPreviousWorkExperiences
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidWorkOnRelease
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreateInductionDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdateInductionDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.aValidNoteDto
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidFutureWorkInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInPrisonInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInductionEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInductionEntityWithJpaFieldsPopulated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidInductionSummaryProjection
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPersonalSkillsAndInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousQualificationsEntityWithJpaFieldsPopulated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousTrainingEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPreviousWorkExperiencesEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidWorkOnReleaseEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.EntityType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.NoteType.INDUCTION
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.aValidNoteEntity
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class InductionEntityMapperTest {

  @InjectMocks
  private lateinit var mapper: InductionEntityMapper

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
    val prisonNumber = randomValidPrisonNumber()
    val prisonId = "BXI"
    val createInductionDto = aValidCreateInductionDto(
      prisonNumber = prisonNumber,
      prisonId = prisonId,
    )
    val expectedFutureWorkInterestsEntity = aValidFutureWorkInterestsEntity()
    val expectedInPrisonInterestsEntity = aValidInPrisonInterestsEntity()
    val expectedPersonalSkillsAndInterestsEntity = aValidPersonalSkillsAndInterestsEntity()
    val expectedPreviousTrainingEntity = aValidPreviousTrainingEntity()
    val expectedPreviousWorkExperiencesEntity = aValidPreviousWorkExperiencesEntity()
    val expectedWorkOnReleaseEntity = aValidWorkOnReleaseEntity()
    val expected = aValidInductionEntity(
      prisonNumber = prisonNumber,
      workOnRelease = expectedWorkOnReleaseEntity,
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
    given(personalSkillsAndInterestsEntityMapper.fromCreateDtoToEntity(any())).willReturn(
      expectedPersonalSkillsAndInterestsEntity,
    )
    given(previousTrainingEntityMapper.fromCreateDtoToEntity(any())).willReturn(expectedPreviousTrainingEntity)
    given(previousWorkExperiencesEntityMapper.fromCreateDtoToEntity(any())).willReturn(
      expectedPreviousWorkExperiencesEntity,
    )
    given(workOnReleaseEntityMapper.fromCreateDtoToEntity(any())).willReturn(expectedWorkOnReleaseEntity)

    // When
    val actual = mapper.fromCreateDtoToEntity(createInductionDto)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(".*reference")
      .isEqualTo(expected)
    verify(futureWorkInterestsEntityMapper).fromCreateDtoToEntity(createInductionDto.futureWorkInterests!!)
    verify(inPrisonInterestsEntityMapper).fromCreateDtoToEntity(createInductionDto.inPrisonInterests!!)
    verify(personalSkillsAndInterestsEntityMapper).fromCreateDtoToEntity(createInductionDto.personalSkillsAndInterests!!)
    verify(previousTrainingEntityMapper).fromCreateDtoToEntity(createInductionDto.previousTraining)
    verify(previousWorkExperiencesEntityMapper).fromCreateDtoToEntity(createInductionDto.previousWorkExperiences!!)
    verify(workOnReleaseEntityMapper).fromCreateDtoToEntity(createInductionDto.workOnRelease)
  }

  @Test
  fun `should map from entity to domain`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val inductionEntity = aValidInductionEntityWithJpaFieldsPopulated(prisonNumber = prisonNumber)
    val previousQualificationsEntity =
      aValidPreviousQualificationsEntityWithJpaFieldsPopulated(prisonNumber = prisonNumber)

    val noteDTO = aValidNoteDto(
      entityReference = inductionEntity.reference,
      noteType = NoteType.INDUCTION,
      entityType = uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.EntityType.INDUCTION,
    )

    val expectedInduction = aFullyPopulatedInduction(
      reference = inductionEntity.reference,
      prisonNumber = prisonNumber,
      workOnRelease = aValidWorkOnRelease(),
      previousQualifications = aValidPreviousQualifications(),
      previousTraining = aValidPreviousTraining(),
      previousWorkExperiences = aValidPreviousWorkExperiences(),
      inPrisonInterests = aValidInPrisonInterests(),
      personalSkillsAndInterests = aValidPersonalSkillsAndInterests(),
      futureWorkInterests = aValidFutureWorkInterests(),
      createdAt = inductionEntity.createdAt!!,
      createdAtPrison = inductionEntity.createdAtPrison,
      createdBy = inductionEntity.createdBy!!,
      lastUpdatedAt = inductionEntity.updatedAt!!,
      lastUpdatedAtPrison = inductionEntity.updatedAtPrison,
      lastUpdatedBy = inductionEntity.updatedBy!!,
      note = noteDTO,
    )

    val noteEntity = aValidNoteEntity(
      prisonNumber = prisonNumber,
      noteType = INDUCTION,
      entityType = EntityType.INDUCTION,
      reference = noteDTO.reference,
      content = noteDTO.content,
      createdAt = noteDTO.createdAt!!,
      createdBy = noteDTO.createdBy!!,
      createdAtPrison = noteDTO.createdAtPrison,
      updatedAt = noteDTO.lastUpdatedAt!!,
      updatedBy = noteDTO.lastUpdatedBy!!,
      updatedAtPrison = noteDTO.lastUpdatedAtPrison,
      entityReference = inductionEntity.reference,
    )

    given(futureWorkInterestsEntityMapper.fromEntityToDomain(any())).willReturn(expectedInduction.futureWorkInterests)
    given(inPrisonInterestsEntityMapper.fromEntityToDomain(any())).willReturn(expectedInduction.inPrisonInterests)
    given(personalSkillsAndInterestsEntityMapper.fromEntityToDomain(any())).willReturn(expectedInduction.personalSkillsAndInterests)
    given(previousTrainingEntityMapper.fromEntityToDomain(any())).willReturn(expectedInduction.previousTraining)
    given(previousWorkExperiencesEntityMapper.fromEntityToDomain(any())).willReturn(expectedInduction.previousWorkExperiences)
    given(workOnReleaseEntityMapper.fromEntityToDomain(any())).willReturn(expectedInduction.workOnRelease)
    given(previousQualificationsEntityMapper.fromEntityToDomain(any())).willReturn(expectedInduction.previousQualifications)

    // When
    val actual = mapper.fromEntityToDomain(inductionEntity, previousQualificationsEntity, noteEntity)

    // Then
    assertThat(actual).isEqualTo(expectedInduction)
    verify(futureWorkInterestsEntityMapper).fromEntityToDomain(inductionEntity.futureWorkInterests!!)
    verify(inPrisonInterestsEntityMapper).fromEntityToDomain(inductionEntity.inPrisonInterests!!)
    verify(personalSkillsAndInterestsEntityMapper).fromEntityToDomain(inductionEntity.personalSkillsAndInterests!!)
    verify(previousTrainingEntityMapper).fromEntityToDomain(inductionEntity.previousTraining)
    verify(previousWorkExperiencesEntityMapper).fromEntityToDomain(inductionEntity.previousWorkExperiences!!)
    verify(workOnReleaseEntityMapper).fromEntityToDomain(inductionEntity.workOnRelease)
    verify(previousQualificationsEntityMapper).fromEntityToDomain(previousQualificationsEntity)
  }

  @Test
  fun `should update induction and call related mappers`() {
    // Given
    val inductionReference = UUID.randomUUID()
    val prisonNumber = randomValidPrisonNumber()
    val existingInductionEntity = aValidInductionEntity(
      reference = inductionReference,
      prisonNumber = prisonNumber,
    )
    val updateInductionDto = aValidUpdateInductionDto(
      reference = inductionReference,
      prisonNumber = prisonNumber,
      prisonId = "MDI",
    )
    val expectedInductionEntity = existingInductionEntity.copy(
      createdAtPrison = "BXI",
      updatedAtPrison = "MDI",
    )

    // When
    mapper.updateEntityFromDto(existingInductionEntity, updateInductionDto)

    // Then
    assertThat(existingInductionEntity).isEqualToComparingAllFields(expectedInductionEntity)
    verify(futureWorkInterestsEntityMapper).updateExistingEntityFromDto(
      existingInductionEntity.futureWorkInterests!!,
      updateInductionDto.futureWorkInterests!!,
    )
    verify(inPrisonInterestsEntityMapper).updateExistingEntityFromDto(
      existingInductionEntity.inPrisonInterests!!,
      updateInductionDto.inPrisonInterests!!,
    )
    verify(personalSkillsAndInterestsEntityMapper).updateExistingEntityFromDto(
      existingInductionEntity.personalSkillsAndInterests!!,
      updateInductionDto.personalSkillsAndInterests!!,
    )
    verify(previousTrainingEntityMapper).updateExistingEntityFromDto(
      existingInductionEntity.previousTraining,
      updateInductionDto.previousTraining!!,
    )
    verify(previousWorkExperiencesEntityMapper).updateExistingEntityFromDto(
      existingInductionEntity.previousWorkExperiences!!,
      updateInductionDto.previousWorkExperiences!!,
    )
    verify(workOnReleaseEntityMapper).updateExistingEntityFromDto(
      existingInductionEntity.workOnRelease,
      updateInductionDto.workOnRelease!!,
    )
  }

  @Test
  fun `should map from entity summaries to domain summaries`() {
    // Given
    val summaryProjection1 = aValidInductionSummaryProjection(prisonNumber = randomValidPrisonNumber())
    val summaryProjection2 = aValidInductionSummaryProjection(prisonNumber = randomValidPrisonNumber())

    val workOnRelease1 = aValidWorkOnRelease()
    val workOnRelease2 = aValidWorkOnRelease()
    given(workOnReleaseEntityMapper.fromEntityToDomain(any())).willReturn(workOnRelease1, workOnRelease2)

    val expected = listOf(
      aValidInductionSummary(
        prisonNumber = summaryProjection1.prisonNumber,
        reference = summaryProjection1.reference,
        workOnRelease = workOnRelease1,
        createdBy = summaryProjection1.createdBy,
        createdAt = summaryProjection1.createdAt,
        lastUpdatedBy = summaryProjection1.updatedBy,
        lastUpdatedAt = summaryProjection1.updatedAt,
      ),
      aValidInductionSummary(
        prisonNumber = summaryProjection2.prisonNumber,
        reference = summaryProjection2.reference,
        workOnRelease = workOnRelease2,
        createdBy = summaryProjection2.createdBy,
        createdAt = summaryProjection2.createdAt,
        lastUpdatedBy = summaryProjection2.updatedBy,
        lastUpdatedAt = summaryProjection2.updatedAt,
      ),
    )

    // When
    val actual = mapper.fromEntitySummariesToDomainSummaries(listOf(summaryProjection1, summaryProjection2))

    // Then
    assertThat(actual).hasSize(2)
    assertThat(actual).isEqualTo(expected)
    verify(workOnReleaseEntityMapper).fromEntityToDomain(summaryProjection1.workOnRelease)
    verify(workOnReleaseEntityMapper).fromEntityToDomain(summaryProjection2.workOnRelease)
  }
}
