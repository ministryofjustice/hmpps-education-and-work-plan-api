package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.mapper

import org.mapstruct.AfterMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.InPrisonInterestsMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.InPrisonTrainingInterestMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.InPrisonWorkInterestMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.InPrisonTrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.InPrisonWorkType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.PrisonWorkAndEducationResponse
import java.time.Instant

@Mapper(
  uses = [
    LocalDateTimeMapper::class,
  ],
  nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
)
abstract class InPrisonInterestsMigrationMapper {

  fun toInPrisonInterestsMigrationEntity(
    prisonId: String,
    inPrisonInterests: PrisonWorkAndEducationResponse?,
  ): InPrisonInterestsMigrationEntity? {
    return if (inPrisonInterests == null) {
      null
    } else {
      createInPrisonInterestsMigrationEntity(prisonId, inPrisonInterests)
    }
  }

  @ExcludeIdField
  @GenerateNewReference
  @Mapping(target = "inPrisonWorkInterests", ignore = true)
  @Mapping(target = "inPrisonTrainingInterests", ignore = true)
  @Mapping(target = "createdAt", source = "inPrisonInterests.modifiedDateTime")
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "createdBy", source = "inPrisonInterests.modifiedBy")
  @Mapping(target = "createdByDisplayName", source = "inPrisonInterests.modifiedBy")
  @Mapping(target = "updatedAt", source = "inPrisonInterests.modifiedDateTime")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "updatedBy", source = "inPrisonInterests.modifiedBy")
  @Mapping(target = "updatedByDisplayName", source = "inPrisonInterests.modifiedBy")
  abstract fun createInPrisonInterestsMigrationEntity(
    prisonId: String,
    inPrisonInterests: PrisonWorkAndEducationResponse?,
  ): InPrisonInterestsMigrationEntity?

  @AfterMapping
  fun addInterests(
    inPrisonInterests: PrisonWorkAndEducationResponse?,
    @MappingTarget entity: InPrisonInterestsMigrationEntity,
  ) {
    inPrisonInterests?.inPrisonWork?.forEach {
      entity.addChild(
        toInPrisonWorkInterestMigrationEntity(it, inPrisonInterests.inPrisonWorkOther, entity.updatedAt!!, entity.updatedBy!!),
        entity.inPrisonWorkInterests(),
      )
    }

    inPrisonInterests?.inPrisonEducation?.forEach {
      entity.addChild(
        toInPrisonTrainingInterestMigrationEntity(it, inPrisonInterests.inPrisonEducationOther, entity.updatedAt!!, entity.updatedBy!!),
        entity.inPrisonTrainingInterests(),
      )
    }
  }

  @ExcludeIdField
  @GenerateNewReference
  @ExcludeParentEntity
  @Mapping(target = "workType", source = "workType")
  @Mapping(target = "workTypeOther", source = "workTypeOther")
  @Mapping(target = "createdAt", source = "modifiedAt")
  @Mapping(target = "createdBy", source = "modifiedBy")
  @Mapping(target = "updatedAt", source = "modifiedAt")
  @Mapping(target = "updatedBy", source = "modifiedBy")
  abstract fun toInPrisonWorkInterestMigrationEntity(
    workType: InPrisonWorkType,
    workTypeOther: String?,
    modifiedAt: Instant,
    modifiedBy: String,
  ): InPrisonWorkInterestMigrationEntity

  @ExcludeIdField
  @GenerateNewReference
  @ExcludeParentEntity
  @Mapping(target = "trainingType", source = "trainingType")
  @Mapping(target = "trainingTypeOther", source = "trainingTypeOther")
  @Mapping(target = "createdAt", source = "modifiedAt")
  @Mapping(target = "createdBy", source = "modifiedBy")
  @Mapping(target = "updatedAt", source = "modifiedAt")
  @Mapping(target = "updatedBy", source = "modifiedBy")
  abstract fun toInPrisonTrainingInterestMigrationEntity(
    trainingType: InPrisonTrainingType,
    trainingTypeOther: String?,
    modifiedAt: Instant,
    modifiedBy: String,
  ): InPrisonTrainingInterestMigrationEntity
}
