package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.Induction
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSummary
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateInductionDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.FutureWorkInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionSummaryProjection
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PersonalSkillsAndInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousQualificationsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousTrainingEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousWorkExperiencesEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkOnReleaseEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFieldsIncludingDisplayNameFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeReferenceField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference

@Mapper(
  uses = [
    FutureWorkInterestsEntityMapper::class,
    InPrisonInterestsEntityMapper::class,
    PersonalSkillsAndInterestsEntityMapper::class,
    PreviousQualificationsEntityMapper::class,
    PreviousTrainingEntityMapper::class,
    PreviousWorkExperiencesEntityMapper::class,
    WorkOnReleaseEntityMapper::class,
  ],
)
abstract class InductionEntityMapper {

  @Autowired
  private lateinit var futureWorkInterestsEntityMapper: FutureWorkInterestsEntityMapper

  @Autowired
  private lateinit var inPrisonInterestsEntityMapper: InPrisonInterestsEntityMapper

  @Autowired
  private lateinit var skillsAndInterestsEntityMapper: PersonalSkillsAndInterestsEntityMapper

  @Autowired
  private lateinit var previousQualificationsEntityMapper: PreviousQualificationsEntityMapper

  @Autowired
  private lateinit var previousTrainingEntityMapper: PreviousTrainingEntityMapper

  @Autowired
  private lateinit var workExperiencesEntityMapper: PreviousWorkExperiencesEntityMapper

  @Autowired
  private lateinit var workOnReleaseEntityMapper: WorkOnReleaseEntityMapper

  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  abstract fun fromCreateDtoToEntity(dto: CreateInductionDto): InductionEntity

  fun fromEntityToDomain(inductionEntity: InductionEntity, previousQualificationsEntity: PreviousQualificationsEntity?): Induction =
    Induction(
      reference = inductionEntity.reference!!,
      prisonNumber = inductionEntity.prisonNumber!!,
      workOnRelease = workOnReleaseEntityMapper.fromEntityToDomain(inductionEntity.workOnRelease),
      previousQualifications = previousQualificationsEntityMapper.fromEntityToDomain(previousQualificationsEntity),
      previousTraining = previousTrainingEntityMapper.fromEntityToDomain(inductionEntity.previousTraining),
      previousWorkExperiences = workExperiencesEntityMapper.fromEntityToDomain(inductionEntity.previousWorkExperiences),
      inPrisonInterests = inPrisonInterestsEntityMapper.fromEntityToDomain(inductionEntity.inPrisonInterests),
      personalSkillsAndInterests = skillsAndInterestsEntityMapper.fromEntityToDomain(inductionEntity.personalSkillsAndInterests),
      futureWorkInterests = futureWorkInterestsEntityMapper.fromEntityToDomain(inductionEntity.futureWorkInterests),
      createdBy = inductionEntity.createdBy,
      createdByDisplayName = inductionEntity.createdByDisplayName,
      createdAt = inductionEntity.createdAt,
      createdAtPrison = inductionEntity.createdAtPrison!!,
      lastUpdatedBy = inductionEntity.updatedBy,
      lastUpdatedByDisplayName = inductionEntity.updatedByDisplayName,
      lastUpdatedAt = inductionEntity.updatedAt,
      lastUpdatedAtPrison = inductionEntity.updatedAtPrison!!,
    )

  @Mapping(target = "lastUpdatedBy", source = "updatedBy")
  @Mapping(target = "lastUpdatedAt", source = "updatedAt")
  abstract fun fromEntitySummaryToDomainSummary(inductionSummaryProjection: InductionSummaryProjection): InductionSummary

  abstract fun fromEntitySummariesToDomainSummaries(inductionSummaryProjections: List<InductionSummaryProjection>): List<InductionSummary>

  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @ExcludeReferenceField
  @Mapping(target = "createdAtPrison", ignore = true)
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "futureWorkInterests", expression = "java( updateFutureWorkInterests(entity, dto) )")
  @Mapping(target = "inPrisonInterests", expression = "java( updateInPrisonInterests(entity, dto) )")
  @Mapping(target = "personalSkillsAndInterests", expression = "java( updatePersonalSkillsAndInterests(entity, dto) )")
  @Mapping(target = "previousTraining", expression = "java( updatePreviousTraining(entity, dto) )")
  @Mapping(target = "previousWorkExperiences", expression = "java( updatePreviousWorkExperiences(entity, dto) )")
  @Mapping(target = "workOnRelease", expression = "java( updateWorkOnRelease(entity, dto) )")
  abstract fun updateEntityFromDto(@MappingTarget entity: InductionEntity, dto: UpdateInductionDto)

  fun updateFutureWorkInterests(entity: InductionEntity, dto: UpdateInductionDto): FutureWorkInterestsEntity? {
    return if (entity.futureWorkInterests == null) {
      // if the Induction previously didn't have FutureWorkInterests (e.g. because the prisoner didn't want to work), then create a new FutureWorkInterests
      futureWorkInterestsEntityMapper.fromUpdateDtoToNewEntity(dto.futureWorkInterests)
    } else {
      // else update the existing FutureWorkInterests
      futureWorkInterestsEntityMapper.updateExistingEntityFromDto(entity.futureWorkInterests!!, dto.futureWorkInterests)
        .let { entity.futureWorkInterests }
    }
  }

  fun updateInPrisonInterests(entity: InductionEntity, dto: UpdateInductionDto): InPrisonInterestsEntity? {
    return if (entity.inPrisonInterests == null) {
      inPrisonInterestsEntityMapper.fromUpdateDtoToNewEntity(dto.inPrisonInterests)
    } else {
      inPrisonInterestsEntityMapper.updateExistingEntityFromDto(entity.inPrisonInterests!!, dto.inPrisonInterests)
        .let { entity.inPrisonInterests }
    }
  }

  fun updatePersonalSkillsAndInterests(entity: InductionEntity, dto: UpdateInductionDto): PersonalSkillsAndInterestsEntity? {
    return if (entity.personalSkillsAndInterests == null) {
      skillsAndInterestsEntityMapper.fromUpdateDtoToNewEntity(dto.personalSkillsAndInterests)
    } else {
      skillsAndInterestsEntityMapper.updateExistingEntityFromDto(entity.personalSkillsAndInterests!!, dto.personalSkillsAndInterests)
        .let { entity.personalSkillsAndInterests }
    }
  }

  fun updatePreviousTraining(entity: InductionEntity, dto: UpdateInductionDto): PreviousTrainingEntity? {
    return if (entity.previousTraining == null) {
      previousTrainingEntityMapper.fromUpdateDtoToNewEntity(dto.previousTraining)
    } else {
      previousTrainingEntityMapper.updateExistingEntityFromDto(entity.previousTraining!!, dto.previousTraining)
        .let { entity.previousTraining }
    }
  }

  fun updatePreviousWorkExperiences(entity: InductionEntity, dto: UpdateInductionDto): PreviousWorkExperiencesEntity? {
    return if (entity.previousWorkExperiences == null) {
      workExperiencesEntityMapper.fromUpdateDtoToNewEntity(dto.previousWorkExperiences)
    } else {
      workExperiencesEntityMapper.updateExistingEntityFromDto(entity.previousWorkExperiences!!, dto.previousWorkExperiences)
        .let { entity.previousWorkExperiences }
    }
  }

  fun updateWorkOnRelease(entity: InductionEntity, dto: UpdateInductionDto): WorkOnReleaseEntity? =
    workOnReleaseEntityMapper.updateExistingEntityFromDto(entity.workOnRelease!!, dto.workOnRelease)
      .let { entity.workOnRelease }
}
