package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousWorkExperiencesEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkExperienceEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFieldsIncludingDisplayNameFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeReferenceField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PreviousWorkExperiences
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkExperience
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.UpdatePreviousWorkExperiencesDto

@Mapper(
  uses = [
    WorkExperienceEntityMapper::class,
  ],
)
abstract class PreviousWorkExperiencesEntityMapper {

  @Autowired
  private lateinit var workExperienceEntityMapper: WorkExperienceEntityMapper

  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  abstract fun fromCreateDtoToEntity(dto: CreatePreviousWorkExperiencesDto): PreviousWorkExperiencesEntity

  @Mapping(target = "lastUpdatedBy", source = "updatedBy")
  @Mapping(target = "lastUpdatedByDisplayName", source = "updatedByDisplayName")
  @Mapping(target = "lastUpdatedAt", source = "updatedAt")
  @Mapping(target = "lastUpdatedAtPrison", source = "updatedAtPrison")
  abstract fun fromEntityToDomain(persistedEntity: PreviousWorkExperiencesEntity): PreviousWorkExperiences

  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @ExcludeReferenceField
  @Mapping(target = "createdAtPrison", ignore = true)
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "experiences", expression = "java( updateWorkExperiences(entity, dto) )")
  abstract fun updateEntityFromDto(
    @MappingTarget entity: PreviousWorkExperiencesEntity?,
    dto: UpdatePreviousWorkExperiencesDto?,
  )

  fun updateWorkExperiences(
    entity: PreviousWorkExperiencesEntity,
    dto: UpdatePreviousWorkExperiencesDto,
  ): List<WorkExperienceEntity> {
    val existingExperiences = entity.experiences ?: mutableListOf()
    val updatedExperiences = dto.experiences ?: mutableListOf()

    updateExistingExperiences(existingExperiences, updatedExperiences)
    addNewExperiences(existingExperiences, updatedExperiences)
    removeExperiences(existingExperiences, updatedExperiences)

    return existingExperiences
  }

  private fun updateExistingExperiences(
    existingExperiences: MutableList<WorkExperienceEntity>,
    updatedExperiences: List<WorkExperience>,
  ) {
    val updatedExperienceTypes = updatedExperiences.map { it.experienceType.name }
    existingExperiences
      .filter { experienceEntity -> updatedExperienceTypes.contains(experienceEntity.experienceType!!.name) }
      .onEach { experienceEntity ->
        workExperienceEntityMapper.updateEntityFromDomain(
          experienceEntity,
          updatedExperiences.first { updatedExperienceDto -> updatedExperienceDto.experienceType.name == experienceEntity.experienceType!!.name },
        )
      }
  }

  private fun addNewExperiences(
    existingExperiences: MutableList<WorkExperienceEntity>,
    updatedExperiences: List<WorkExperience>,
  ) {
    val currentExperienceTypes = existingExperiences.map { it.experienceType!!.name }
    existingExperiences.addAll(
      updatedExperiences
        .filter { updatedExperienceDto -> !currentExperienceTypes.contains(updatedExperienceDto.experienceType.name) }
        .map { newExperienceDto -> workExperienceEntityMapper.fromDomainToEntity(newExperienceDto) },
    )
  }

  /**
   * Remove any [WorkInterestEntity]s whose work type is not in the list of updated [WorkInterest]s.
   */
  private fun removeExperiences(
    existingExperiences: MutableList<WorkExperienceEntity>,
    updatedExperiences: List<WorkExperience>,
  ) {
    val updatedExperienceTypes = updatedExperiences.map { it.experienceType.name }
    existingExperiences.removeIf { experienceEntity ->
      !updatedExperienceTypes.contains(experienceEntity.experienceType!!.name)
    }
  }
}

@Mapper
interface WorkExperienceEntityMapper {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  fun fromDomainToEntity(domain: WorkExperience): WorkExperienceEntity

  fun fromEntityToDomain(persistedEntity: WorkExperienceEntity): WorkExperience

  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  fun updateEntityFromDomain(@MappingTarget entity: WorkExperienceEntity, domain: WorkExperience)
}
