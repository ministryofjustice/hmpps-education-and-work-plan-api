package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.mapper

import org.mapstruct.AfterMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.PersonalInterestMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.PersonalSkillMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.PersonalSkillsAndInterestsMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.PersonalInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.PersonalSkill
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.SkillsAndInterestsResponse
import java.time.Instant

@Mapper(
  uses = [
    LocalDateTimeMapper::class,
  ],
  nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
)
abstract class SkillsAndInterestsMigrationMapper {

  fun toPersonalSkillsAndInterestsMigrationEntity(
    prisonId: String,
    skillsAndInterests: SkillsAndInterestsResponse?,
  ): PersonalSkillsAndInterestsMigrationEntity? {
    return if (skillsAndInterests == null) {
      null
    } else {
      createPersonalSkillsAndInterestsMigrationEntity(prisonId, skillsAndInterests)
    }
  }

  @ExcludeIdField
  @GenerateNewReference
  @Mapping(target = "skills", ignore = true)
  @Mapping(target = "interests", ignore = true)
  @Mapping(target = "createdAt", source = "skillsAndInterests.modifiedDateTime")
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "createdBy", source = "skillsAndInterests.modifiedBy")
  @Mapping(target = "createdByDisplayName", source = "skillsAndInterests.modifiedBy")
  @Mapping(target = "updatedAt", source = "skillsAndInterests.modifiedDateTime")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "updatedBy", source = "skillsAndInterests.modifiedBy")
  @Mapping(target = "updatedByDisplayName", source = "skillsAndInterests.modifiedBy")
  abstract fun createPersonalSkillsAndInterestsMigrationEntity(
    prisonId: String,
    skillsAndInterests: SkillsAndInterestsResponse?,
  ): PersonalSkillsAndInterestsMigrationEntity?

  @AfterMapping
  fun addSkillsAndInterests(
    skillsAndInterests: SkillsAndInterestsResponse?,
    @MappingTarget entity: PersonalSkillsAndInterestsMigrationEntity,
  ) {
    skillsAndInterests?.skills?.forEach {
      entity.addChild(
        toPersonalSkillMigrationEntity(it, skillsAndInterests.skillsOther, entity.updatedAt!!, entity.updatedBy!!),
        entity.skills(),
      )
    }

    skillsAndInterests?.personalInterests?.forEach {
      entity.addChild(
        toPersonalInterestMigrationEntity(it, skillsAndInterests.personalInterestsOther, entity.updatedAt!!, entity.updatedBy!!),
        entity.interests(),
      )
    }
  }

  @ExcludeIdField
  @GenerateNewReference
  @ExcludeParentEntity
  @Mapping(target = "skillType", source = "skillType")
  @Mapping(target = "skillTypeOther", source = "skillTypeOther")
  @Mapping(target = "createdAt", source = "modifiedAt")
  @Mapping(target = "createdBy", source = "modifiedBy")
  @Mapping(target = "updatedAt", source = "modifiedAt")
  @Mapping(target = "updatedBy", source = "modifiedBy")
  abstract fun toPersonalSkillMigrationEntity(
    skillType: PersonalSkill,
    skillTypeOther: String?,
    modifiedAt: Instant,
    modifiedBy: String,
  ): PersonalSkillMigrationEntity

  @ExcludeIdField
  @GenerateNewReference
  @ExcludeParentEntity
  @Mapping(target = "interestType", source = "interestType")
  @Mapping(target = "interestTypeOther", source = "interestTypeOther")
  @Mapping(target = "createdAt", source = "modifiedAt")
  @Mapping(target = "createdBy", source = "modifiedBy")
  @Mapping(target = "updatedAt", source = "modifiedAt")
  @Mapping(target = "updatedBy", source = "modifiedBy")
  abstract fun toPersonalInterestMigrationEntity(
    interestType: PersonalInterest,
    interestTypeOther: String?,
    modifiedAt: Instant,
    modifiedBy: String,
  ): PersonalInterestMigrationEntity
}
