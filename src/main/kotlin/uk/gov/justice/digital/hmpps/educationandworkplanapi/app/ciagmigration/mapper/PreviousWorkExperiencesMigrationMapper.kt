package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.mapper

import org.mapstruct.AfterMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.PreviousWorkExperiencesMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.WorkExperienceMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.PreviousWorkResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.WorkExperience
import java.time.Instant

@Mapper(
  uses = [
    LocalDateTimeMapper::class,
  ],
  nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
)
abstract class PreviousWorkExperiencesMigrationMapper {

  fun toPreviousWorkExperiencesMigrationEntity(
    prisonId: String,
    workExperienceResponse: PreviousWorkResponse?,
  ): PreviousWorkExperiencesMigrationEntity? {
    return if (workExperienceResponse == null) {
      null
    } else {
      createPreviousWorkExperiencesMigrationEntity(prisonId, workExperienceResponse)
    }
  }

  @ExcludeIdField
  @GenerateNewReference
  @Mapping(target = "experiences", ignore = true)
  @Mapping(target = "createdAt", source = "workExperienceResponse.modifiedDateTime")
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "createdBy", source = "workExperienceResponse.modifiedBy")
  @Mapping(target = "createdByDisplayName", source = "workExperienceResponse.modifiedBy")
  @Mapping(target = "updatedAt", source = "workExperienceResponse.modifiedDateTime")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "updatedBy", source = "workExperienceResponse.modifiedBy")
  @Mapping(target = "updatedByDisplayName", source = "workExperienceResponse.modifiedBy")
  abstract fun createPreviousWorkExperiencesMigrationEntity(
    prisonId: String,
    workExperienceResponse: PreviousWorkResponse?,
  ): PreviousWorkExperiencesMigrationEntity?

  @AfterMapping
  fun addExperiences(
    workExperienceResponse: PreviousWorkResponse?,
    @MappingTarget entity: PreviousWorkExperiencesMigrationEntity,
  ) {
    workExperienceResponse?.workExperience?.forEach {
      entity.addChild(
        toWorkExperienceMigrationEntity(it, entity.updatedAt!!, entity.updatedBy!!),
        entity.experiences(),
      )
    }
  }

  @ExcludeIdField
  @GenerateNewReference
  @ExcludeParentEntity
  @Mapping(target = "experienceType", source = "workExperience.typeOfWorkExperience")
  @Mapping(target = "experienceTypeOther", source = "workExperience.otherWork")
  @Mapping(target = "role", source = "workExperience.role")
  @Mapping(target = "details", source = "workExperience.details")
  @Mapping(target = "createdAt", source = "modifiedAt")
  @Mapping(target = "createdBy", source = "modifiedBy")
  @Mapping(target = "updatedAt", source = "modifiedAt")
  @Mapping(target = "updatedBy", source = "modifiedBy")
  abstract fun toWorkExperienceMigrationEntity(
    workExperience: WorkExperience,
    modifiedAt: Instant,
    modifiedBy: String,
  ): WorkExperienceMigrationEntity
}
