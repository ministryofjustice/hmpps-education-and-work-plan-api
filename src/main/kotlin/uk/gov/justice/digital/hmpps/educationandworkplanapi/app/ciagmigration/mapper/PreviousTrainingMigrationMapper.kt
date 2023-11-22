package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.PreviousTrainingMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.EducationAndQualificationResponse

@Mapper(
  uses = [
    LocalDateTimeMapper::class,
  ],
  nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
)
abstract class PreviousTrainingMigrationMapper {

  fun toPreviousTrainingMigrationEntity(
    prisonId: String,
    ciagTrainingResponse: EducationAndQualificationResponse?,
  ): PreviousTrainingMigrationEntity? {
    return if (ciagTrainingResponse == null) {
      null
    } else {
      createPreviousTrainingMigrationEntity(prisonId, ciagTrainingResponse)
    }
  }

  @ExcludeIdField
  @GenerateNewReference
  @Mapping(target = "trainingTypes", source = "ciagTrainingResponse.additionalTraining")
  @Mapping(target = "trainingTypeOther", source = "ciagTrainingResponse.additionalTrainingOther")
  @Mapping(target = "createdAt", source = "ciagTrainingResponse.modifiedDateTime")
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "createdBy", source = "ciagTrainingResponse.modifiedBy")
  @Mapping(target = "createdByDisplayName", source = "ciagTrainingResponse.modifiedBy")
  @Mapping(target = "updatedAt", source = "ciagTrainingResponse.modifiedDateTime")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "updatedBy", source = "ciagTrainingResponse.modifiedBy")
  @Mapping(target = "updatedByDisplayName", source = "ciagTrainingResponse.modifiedBy")
  abstract fun createPreviousTrainingMigrationEntity(
    prisonId: String,
    ciagTrainingResponse: EducationAndQualificationResponse?,
  ): PreviousTrainingMigrationEntity?
}
