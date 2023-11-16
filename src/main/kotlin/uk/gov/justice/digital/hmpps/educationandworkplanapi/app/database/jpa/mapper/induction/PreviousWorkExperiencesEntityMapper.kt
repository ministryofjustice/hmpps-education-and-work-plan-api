package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.AfterMapping
import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.Named
import org.mapstruct.NullValueMappingStrategy
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousWorkExperiencesEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkExperienceEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFieldsIncludingDisplayNameFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeParentEntity
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
  nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
)
abstract class PreviousWorkExperiencesEntityMapper {

  @Autowired
  private lateinit var workExperienceEntityMapper: WorkExperienceEntityMapper

  @Autowired
  private lateinit var entityListManager: InductionEntityListManager<WorkExperienceEntity, WorkExperience>

  @BeanMapping(qualifiedByName = ["addNewExperiencesDuringCreate"])
  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "experiences", ignore = true)
  abstract fun fromCreateDtoToEntity(dto: CreatePreviousWorkExperiencesDto): PreviousWorkExperiencesEntity

  @Named("addNewExperiencesDuringCreate")
  @AfterMapping
  fun addNewExperiencesDuringCreate(dto: CreatePreviousWorkExperiencesDto, @MappingTarget entity: PreviousWorkExperiencesEntity) {
    addNewExperiences(dto.experiences, entity)
  }

  @Mapping(target = "lastUpdatedBy", source = "updatedBy")
  @Mapping(target = "lastUpdatedByDisplayName", source = "updatedByDisplayName")
  @Mapping(target = "lastUpdatedAt", source = "updatedAt")
  @Mapping(target = "lastUpdatedAtPrison", source = "updatedAtPrison")
  @Mapping(target = "experiences", source = "experiences")
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
    entityListManager.addNew(entity, existingExperiences, updatedExperiences, workExperienceEntityMapper)
    entityListManager.deleteRemoved(existingExperiences, updatedExperiences)

    return existingExperiences
  }

  @BeanMapping(qualifiedByName = ["addNewExperiencesDuringUpdate"])
  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "experiences", ignore = true)
  abstract fun fromUpdateDtoToEntity(previousWorkExperiences: UpdatePreviousWorkExperiencesDto?): PreviousWorkExperiencesEntity?

  @Named("addNewExperiencesDuringUpdate")
  @AfterMapping
  fun addNewExperiencesDuringUpdate(dto: UpdatePreviousWorkExperiencesDto, @MappingTarget entity: PreviousWorkExperiencesEntity) {
    addNewExperiences(dto.experiences, entity)
  }

  private fun addNewExperiences(experiences: List<WorkExperience>, entity: PreviousWorkExperiencesEntity) {
    experiences.forEach {
      entity.addChild(
        workExperienceEntityMapper.fromDomainToEntity(it),
        entity.experiences(),
      )
    }
  }
}

@Mapper
interface WorkExperienceEntityMapper : KeyAwareEntityMapper<WorkExperienceEntity, WorkExperience> {
  @ExcludeJpaManagedFields
  @GenerateNewReference
  @ExcludeParentEntity
  override fun fromDomainToEntity(domain: WorkExperience): WorkExperienceEntity

  fun fromEntityToDomain(persistedEntity: WorkExperienceEntity): WorkExperience

  @ExcludeJpaManagedFields
  @ExcludeReferenceField
  @ExcludeParentEntity
  override fun updateEntityFromDomain(@MappingTarget entity: WorkExperienceEntity, domain: WorkExperience)
}
