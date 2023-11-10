package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousWorkExperiencesEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkExperienceEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFieldsIncludingDisplayNameFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeReferenceField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PreviousWorkExperiences
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkExperience
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

  @Autowired
  private lateinit var entityListManager: InductionEntityListManager<WorkExperienceEntity, WorkExperience>

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
    val existingExperiences = entity.experiences!!
    val updatedExperiences = dto.experiences

    entityListManager.updateExisting(existingExperiences, updatedExperiences, workExperienceEntityMapper)
    entityListManager.addNew(existingExperiences, updatedExperiences, workExperienceEntityMapper)
    entityListManager.deleteRemoved(existingExperiences, updatedExperiences)

    return existingExperiences
  }
}

@Mapper
interface WorkExperienceEntityMapper : KeyAwareEntityMapper<WorkExperienceEntity, WorkExperience> {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  override fun fromDomainToEntity(domain: WorkExperience): WorkExperienceEntity

  fun fromEntityToDomain(persistedEntity: WorkExperienceEntity): WorkExperience

  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  override fun updateEntityFromDomain(@MappingTarget entity: WorkExperienceEntity, domain: WorkExperience)
}
