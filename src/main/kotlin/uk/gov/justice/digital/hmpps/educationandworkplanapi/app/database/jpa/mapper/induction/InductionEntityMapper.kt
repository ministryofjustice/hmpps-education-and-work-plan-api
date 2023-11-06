package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.FutureWorkInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InPrisonInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PersonalSkillsAndInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousQualificationsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousTrainingEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousWorkExperiencesEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkOnReleaseEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFieldsIncludingDisplayNameFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeReferenceField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.Induction
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateInductionDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.UpdateInductionDto

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
  private lateinit var personalSkillsAndInterestsEntityMapper: PersonalSkillsAndInterestsEntityMapper

  @Autowired
  private lateinit var previousQualificationsEntityMapper: PreviousQualificationsEntityMapper

  @Autowired
  private lateinit var previousTrainingEntityMapper: PreviousTrainingEntityMapper

  @Autowired
  private lateinit var previousWorkExperiencesEntityMapper: PreviousWorkExperiencesEntityMapper

  @Autowired
  private lateinit var workOnReleaseEntityMapper: WorkOnReleaseEntityMapper

  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  abstract fun fromCreateDtoToEntity(dto: CreateInductionDto): InductionEntity

  @Mapping(target = "lastUpdatedBy", source = "updatedBy")
  @Mapping(target = "lastUpdatedByDisplayName", source = "updatedByDisplayName")
  @Mapping(target = "lastUpdatedAt", source = "updatedAt")
  @Mapping(target = "lastUpdatedAtPrison", source = "updatedAtPrison")
  abstract fun fromEntityToDomain(persistedEntity: InductionEntity): Induction

  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @ExcludeReferenceField
  @Mapping(target = "createdAtPrison", ignore = true)
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "futureWorkInterests", expression = "java( updateFutureWorkInterests(entity, dto) )")
  @Mapping(target = "inPrisonInterests", expression = "java( updateInPrisonInterests(entity, dto) )")
  @Mapping(target = "personalSkillsAndInterests", expression = "java( updatePersonalSkillsAndInterests(entity, dto) )")
  @Mapping(target = "previousQualifications", expression = "java( updatePreviousQualifications(entity, dto) )")
  @Mapping(target = "previousTraining", expression = "java( updatePreviousTraining(entity, dto) )")
  @Mapping(target = "previousWorkExperiences", expression = "java( updatePreviousWorkExperiences(entity, dto) )")
  @Mapping(target = "workOnRelease", expression = "java( updateWorkOnRelease(entity, dto) )")
  abstract fun updateEntityFromDto(@MappingTarget entity: InductionEntity, dto: UpdateInductionDto)

  fun updateFutureWorkInterests(entity: InductionEntity, dto: UpdateInductionDto): FutureWorkInterestsEntity? =
    futureWorkInterestsEntityMapper.updateEntityFromDto(entity.futureWorkInterests, dto.futureWorkInterests)
      .let { entity.futureWorkInterests }

  fun updateInPrisonInterests(entity: InductionEntity, dto: UpdateInductionDto): InPrisonInterestsEntity? =
    inPrisonInterestsEntityMapper.updateEntityFromDto(entity.inPrisonInterests, dto.inPrisonInterests)
      .let { entity.inPrisonInterests }

  fun updatePersonalSkillsAndInterests(entity: InductionEntity, dto: UpdateInductionDto): PersonalSkillsAndInterestsEntity? =
    personalSkillsAndInterestsEntityMapper.updateEntityFromDto(entity.personalSkillsAndInterests, dto.personalSkillsAndInterests)
      .let { entity.personalSkillsAndInterests }

  fun updatePreviousQualifications(entity: InductionEntity, dto: UpdateInductionDto): PreviousQualificationsEntity? =
    previousQualificationsEntityMapper.updateEntityFromDto(entity.previousQualifications, dto.previousQualifications)
      .let { entity.previousQualifications }

  fun updatePreviousTraining(entity: InductionEntity, dto: UpdateInductionDto): PreviousTrainingEntity? =
    previousTrainingEntityMapper.updateEntityFromDto(entity.previousTraining, dto.previousTraining)
      .let { entity.previousTraining }

  fun updatePreviousWorkExperiences(entity: InductionEntity, dto: UpdateInductionDto): PreviousWorkExperiencesEntity? =
    previousWorkExperiencesEntityMapper.updateEntityFromDto(entity.previousWorkExperiences, dto.previousWorkExperiences)
      .let { entity.previousWorkExperiences }

  fun updateWorkOnRelease(entity: InductionEntity, dto: UpdateInductionDto): WorkOnReleaseEntity? =
    workOnReleaseEntityMapper.updateEntityFromDto(entity.workOnRelease, dto.workOnRelease)
      .let { entity.workOnRelease }
}
