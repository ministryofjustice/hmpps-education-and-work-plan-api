package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousTrainingEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeJpaManagedFieldsIncludingDisplayNameFields
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ExcludeReferenceField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GenerateNewReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PreviousTraining
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.UpdatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.TrainingType as TrainingTypeEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.TrainingType as TrainingTypeDomain

@Mapper(
  uses = [
    TrainingTypeMapper::class,
  ],
)
interface PreviousTrainingEntityMapper {

  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  fun fromCreateDtoToEntity(dto: CreatePreviousTrainingDto): PreviousTrainingEntity

  @Mapping(target = "lastUpdatedBy", source = "updatedBy")
  @Mapping(target = "lastUpdatedByDisplayName", source = "updatedByDisplayName")
  @Mapping(target = "lastUpdatedAt", source = "updatedAt")
  @Mapping(target = "lastUpdatedAtPrison", source = "updatedAtPrison")
  fun fromEntityToDomain(persistedEntity: PreviousTrainingEntity): PreviousTraining

  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @ExcludeReferenceField
  @Mapping(target = "createdAtPrison", ignore = true)
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  fun updateEntityFromDto(@MappingTarget entity: PreviousTrainingEntity?, dto: UpdatePreviousTrainingDto?)

  @ExcludeJpaManagedFieldsIncludingDisplayNameFields
  @GenerateNewReference
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  fun fromUpdateDtoToEntity(previousTraining: UpdatePreviousTrainingDto?): PreviousTrainingEntity?
}

@Mapper
interface TrainingTypeMapper {
  fun fromDomainToEntity(domainEnumList: List<TrainingTypeDomain>): List<TrainingTypeEntity>

  fun fromEntityToDomain(persistedEntities: List<TrainingTypeEntity>): List<TrainingTypeDomain>
}
