package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.mapper

import org.mapstruct.AfterMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.PreviousQualificationsMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.QualificationMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.AchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.EducationAndQualificationResponse
import java.time.Instant

@Mapper(
  uses = [
    LocalDateTimeMapper::class,
  ],
  nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
)
abstract class PreviousQualificationsMigrationMapper {

  fun toPreviousQualificationsMigrationEntity(
    prisonId: String,
    ciagEducationResponse: EducationAndQualificationResponse?,
  ): PreviousQualificationsMigrationEntity? {
    return if (ciagEducationResponse?.educationLevel == null) {
      null
    } else {
      createPreviousQualificationsMigrationEntity(prisonId, ciagEducationResponse)
    }
  }

  @ExcludeIdField
  @GenerateNewReference
  @Mapping(target = "educationLevel", source = "ciagEducationResponse.educationLevel")
  @Mapping(target = "qualifications", ignore = true)
  @Mapping(target = "createdAt", source = "ciagEducationResponse.modifiedDateTime")
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "createdBy", source = "ciagEducationResponse.modifiedBy")
  @Mapping(target = "createdByDisplayName", source = "ciagEducationResponse.modifiedBy")
  @Mapping(target = "updatedAt", source = "ciagEducationResponse.modifiedDateTime")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "updatedBy", source = "ciagEducationResponse.modifiedBy")
  @Mapping(target = "updatedByDisplayName", source = "ciagEducationResponse.modifiedBy")
  abstract fun createPreviousQualificationsMigrationEntity(
    prisonId: String,
    ciagEducationResponse: EducationAndQualificationResponse?,
  ): PreviousQualificationsMigrationEntity?

  @AfterMapping
  fun addQualifications(
    educationAndQualificationResponse: EducationAndQualificationResponse?,
    @MappingTarget entity: PreviousQualificationsMigrationEntity,
  ) {
    educationAndQualificationResponse?.qualifications?.forEach {
      entity.addChild(
        toQualificationMigrationEntity(it, entity.updatedAt!!, entity.updatedBy!!),
        entity.qualifications(),
      )
    }
  }

  @ExcludeIdField
  @GenerateNewReference
  @ExcludeParentEntity
  @Mapping(target = "subject", source = "qualification.subject")
  @Mapping(target = "level", source = "qualification.level")
  @Mapping(target = "grade", source = "qualification.grade")
  @Mapping(target = "createdAt", source = "modifiedAt")
  @Mapping(target = "createdBy", source = "modifiedBy")
  @Mapping(target = "updatedAt", source = "modifiedAt")
  @Mapping(target = "updatedBy", source = "modifiedBy")
  abstract fun toQualificationMigrationEntity(
    qualification: AchievedQualification,
    modifiedAt: Instant,
    modifiedBy: String,
  ): QualificationMigrationEntity
}
