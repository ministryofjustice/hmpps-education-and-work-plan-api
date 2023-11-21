package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.mapper

import org.mapstruct.AfterMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.FutureWorkInterestsMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.WorkInterestMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.WorkInterestDetail
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.WorkInterestsResponse
import java.time.Instant

@Mapper(
  uses = [
    LocalDateTimeMapper::class,
  ],
  nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
)
abstract class FutureWorkInterestsMigrationMapper {

  fun toFutureWorkInterestsMigrationEntity(
    prisonId: String,
    workInterests: WorkInterestsResponse?,
  ): FutureWorkInterestsMigrationEntity? {
    return if (workInterests == null) {
      null
    } else {
      createFutureWorkInterestsMigrationEntity(prisonId, workInterests)
    }
  }

  @ExcludeIdField
  @GenerateNewReference
  @Mapping(target = "interests", ignore = true)
  @Mapping(target = "createdAt", source = "workInterests.modifiedDateTime")
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "createdBy", source = "workInterests.modifiedBy")
  @Mapping(target = "createdByDisplayName", source = "workInterests.modifiedBy")
  @Mapping(target = "updatedAt", source = "workInterests.modifiedDateTime")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "updatedBy", source = "workInterests.modifiedBy")
  @Mapping(target = "updatedByDisplayName", source = "workInterests.modifiedBy")
  abstract fun createFutureWorkInterestsMigrationEntity(
    prisonId: String,
    workInterests: WorkInterestsResponse?,
  ): FutureWorkInterestsMigrationEntity?

  @AfterMapping
  fun addExperiences(
    workInterests: WorkInterestsResponse?,
    @MappingTarget entity: FutureWorkInterestsMigrationEntity,
  ) {
    workInterests?.particularJobInterests?.forEach {
      entity.addChild(
        toWorkInterestMigrationEntity(it, workInterests.workInterestsOther, entity.updatedAt!!, entity.updatedBy!!),
        entity.interests(),
      )
    }
  }

  @ExcludeIdField
  @GenerateNewReference
  @ExcludeParentEntity
  @Mapping(target = "workType", source = "workInterestDetail.workInterest")
  @Mapping(target = "workTypeOther", source = "workInterestOther")
  @Mapping(target = "role", source = "workInterestDetail.role")
  @Mapping(target = "createdAt", source = "modifiedAt")
  @Mapping(target = "createdBy", source = "modifiedBy")
  @Mapping(target = "updatedAt", source = "modifiedAt")
  @Mapping(target = "updatedBy", source = "modifiedBy")
  abstract fun toWorkInterestMigrationEntity(
    workInterestDetail: WorkInterestDetail,
    workInterestOther: String?,
    modifiedAt: Instant,
    modifiedBy: String,
  ): WorkInterestMigrationEntity
}
