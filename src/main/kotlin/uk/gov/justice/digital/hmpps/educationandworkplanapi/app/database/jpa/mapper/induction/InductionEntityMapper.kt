package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.Induction
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSummary
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateInductionDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionSummaryProjection
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousQualificationsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.NoteEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.note.NoteMapper
import java.util.UUID

@Component
class InductionEntityMapper(
  private val personalSkillsAndInterestsEntityMapper: PersonalSkillsAndInterestsEntityMapper,
  private val previousWorkExperiencesEntityMapper: PreviousWorkExperiencesEntityMapper,
  private val futureWorkInterestsEntityMapper: FutureWorkInterestsEntityMapper,
  private val inPrisonInterestsEntityMapper: InPrisonInterestsEntityMapper,
  private val skillsAndInterestsEntityMapper: PersonalSkillsAndInterestsEntityMapper,
  private val previousQualificationsEntityMapper: PreviousQualificationsEntityMapper,
  private val previousTrainingEntityMapper: PreviousTrainingEntityMapper,
  private val workExperiencesEntityMapper: PreviousWorkExperiencesEntityMapper,
  private val workOnReleaseEntityMapper: WorkOnReleaseEntityMapper,
) {
  fun fromCreateDtoToEntity(dto: CreateInductionDto): InductionEntity = with(dto) {
    InductionEntity(
      reference = UUID.randomUUID(),
      prisonNumber = prisonNumber,
      workOnRelease = workOnReleaseEntityMapper.fromCreateDtoToEntity(workOnRelease),
      previousTraining = previousTrainingEntityMapper.fromCreateDtoToEntity(previousTraining),
      previousWorkExperiences = previousWorkExperiences?.let {
        previousWorkExperiencesEntityMapper.fromCreateDtoToEntity(it)
      },
      inPrisonInterests = inPrisonInterests?.let { inPrisonInterestsEntityMapper.fromCreateDtoToEntity(it) },
      personalSkillsAndInterests = personalSkillsAndInterests?.let {
        personalSkillsAndInterestsEntityMapper.fromCreateDtoToEntity(it)
      },
      futureWorkInterests = futureWorkInterests?.let { futureWorkInterestsEntityMapper.fromCreateDtoToEntity(it) },
      conductedBy = conductedBy,
      conductedByRole = conductedByRole,
      completedDate = conductedAt,
      createdAtPrison = prisonId,
      updatedAtPrison = prisonId,
    )
  }

  fun fromEntityToDomain(
    inductionEntity: InductionEntity,
    previousQualificationsEntity: PreviousQualificationsEntity?,
    noteEntity: NoteEntity? = null,
  ): Induction = Induction(
    reference = inductionEntity.reference,
    prisonNumber = inductionEntity.prisonNumber,
    workOnRelease = workOnReleaseEntityMapper.fromEntityToDomain(inductionEntity.workOnRelease),
    previousQualifications = previousQualificationsEntity?.let {
      previousQualificationsEntityMapper.fromEntityToDomain(it)
    },
    previousTraining = previousTrainingEntityMapper.fromEntityToDomain(inductionEntity.previousTraining),
    previousWorkExperiences = workExperiencesEntityMapper.fromEntityToDomain(inductionEntity.previousWorkExperiences),
    inPrisonInterests = inPrisonInterestsEntityMapper.fromEntityToDomain(inductionEntity.inPrisonInterests),
    personalSkillsAndInterests = skillsAndInterestsEntityMapper.fromEntityToDomain(inductionEntity.personalSkillsAndInterests),
    futureWorkInterests = futureWorkInterestsEntityMapper.fromEntityToDomain(inductionEntity.futureWorkInterests),
    createdBy = inductionEntity.createdBy,
    createdAt = inductionEntity.createdAt,
    createdAtPrison = inductionEntity.createdAtPrison,
    lastUpdatedBy = inductionEntity.updatedBy,
    lastUpdatedAt = inductionEntity.updatedAt,
    lastUpdatedAtPrison = inductionEntity.updatedAtPrison,
    conductedBy = inductionEntity.conductedBy,
    conductedByRole = inductionEntity.conductedByRole,
    completedDate = inductionEntity.completedDate,
    note = noteEntity?.let { NoteMapper.fromEntityToDomain(it) },
  )

  fun fromEntitySummariesToDomainSummaries(inductionSummaryProjections: List<InductionSummaryProjection>): List<InductionSummary> = inductionSummaryProjections.map {
    InductionSummary(
      reference = it.reference,
      prisonNumber = it.prisonNumber,
      workOnRelease = workOnReleaseEntityMapper.fromEntityToDomain(it.workOnRelease),
      createdBy = it.createdBy,
      createdAt = it.createdAt,
      lastUpdatedBy = it.updatedBy,
      lastUpdatedAt = it.updatedAt,
    )
  }

  fun updateEntityFromDto(entity: InductionEntity, dto: UpdateInductionDto) = with(entity) {
    updatedAtPrison = dto.prisonId

    updateWorkOnRelease(this, dto.workOnRelease)
    updatePreviousTraining(this, dto.previousTraining)
    updateFutureWorkInterests(this, dto.futureWorkInterests)
    updateInPrisonInterests(this, dto.inPrisonInterests)
    updatePersonalSkillsAndInterests(this, dto.personalSkillsAndInterests)
    updatePreviousWorkExperiences(this, dto.previousWorkExperiences)
  }

  private fun updateFutureWorkInterests(entity: InductionEntity, dto: UpdateFutureWorkInterestsDto?) = dto?.also {
    entity.futureWorkInterests
      ?.also { existingWorkInterests ->
        // update the existing FutureWorkInterests
        futureWorkInterestsEntityMapper.updateExistingEntityFromDto(existingWorkInterests, it)
      }
      ?: run {
        // if the Induction previously didn't have FutureWorkInterests (e.g. because the prisoner originally didn't want to work), then create a new FutureWorkInterests
        entity.futureWorkInterests = futureWorkInterestsEntityMapper.fromUpdateDtoToNewEntity(it)
      }
  }

  private fun updateInPrisonInterests(entity: InductionEntity, dto: UpdateInPrisonInterestsDto?) = dto?.also {
    entity.inPrisonInterests
      ?.also { existingInPrisonInterests ->
        // update the existing InPrisonInterests
        inPrisonInterestsEntityMapper.updateExistingEntityFromDto(existingInPrisonInterests, it)
      }
      ?: run {
        // if the Induction previously didn't have InPrisonInterests, then create a new InPrisonInterests
        entity.inPrisonInterests = inPrisonInterestsEntityMapper.fromUpdateDtoToNewEntity(it)
      }
  }

  private fun updatePersonalSkillsAndInterests(entity: InductionEntity, dto: UpdatePersonalSkillsAndInterestsDto?) = dto?.also {
    entity.personalSkillsAndInterests
      ?.also { existingPersonalSkillsAndInterests ->
        // Update the existing Personal Skills And Interests
        skillsAndInterestsEntityMapper.updateExistingEntityFromDto(existingPersonalSkillsAndInterests, it)
      }
      ?: run {
        // If the Induction previously didn't have Personal Skills And Interests, then create a new PersonalSkillsAndInterests
        entity.personalSkillsAndInterests = skillsAndInterestsEntityMapper.fromUpdateDtoToNewEntity(it)
      }
  }

  private fun updatePreviousTraining(entity: InductionEntity, dto: UpdatePreviousTrainingDto?) = dto?.also {
    previousTrainingEntityMapper.updateExistingEntityFromDto(entity.previousTraining, it)
  }

  private fun updatePreviousWorkExperiences(entity: InductionEntity, dto: UpdatePreviousWorkExperiencesDto?) = dto?.also {
    entity.previousWorkExperiences
      ?.also { existingPreviousWorkExperiences ->
        // Update the existing PreviousWorkExperiences
        previousWorkExperiencesEntityMapper.updateExistingEntityFromDto(existingPreviousWorkExperiences, it)
      }
      ?: run {
        // If the Induction previously didn't have PreviousWorkExperiences, then create a PreviousWorkExperiences
        entity.previousWorkExperiences = previousWorkExperiencesEntityMapper.fromUpdateDtoToNewEntity(it)
      }
  }

  private fun updateWorkOnRelease(entity: InductionEntity, dto: UpdateWorkOnReleaseDto?) = dto?.also {
    workOnReleaseEntityMapper.updateExistingEntityFromDto(entity.workOnRelease, it)
  }
}
