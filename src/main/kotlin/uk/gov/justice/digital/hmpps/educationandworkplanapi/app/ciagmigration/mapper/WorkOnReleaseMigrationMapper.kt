package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.AffectAbilityToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.NotHopingToWorkReason
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.WorkOnReleaseMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.AbilityToWorkFactor
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.CiagInductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.ReasonNotToWork

@Mapper(
  uses = [
    LocalDateTimeMapper::class,
  ],
  nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
)
interface WorkOnReleaseMigrationMapper {

  @ExcludeIdField
  @GenerateNewReference
  @Mapping(target = "hopingToWork", source = "ciagInduction.hopingToGetWork")
  @Mapping(target = "notHopingToWorkReasons", source = "ciagInduction.reasonToNotGetWork")
  @Mapping(target = "notHopingToWorkOtherReason", source = "ciagInduction.reasonToNotGetWorkOther")
  @Mapping(target = "affectAbilityToWork", source = "ciagInduction.abilityToWork")
  @Mapping(target = "affectAbilityToWorkOther", source = "ciagInduction.abilityToWorkOther")
  @Mapping(target = "createdAt", source = "ciagInduction.createdDateTime")
  @Mapping(target = "createdAtPrison", source = "prisonId")
  @Mapping(target = "createdBy", source = "ciagInduction.createdBy")
  @Mapping(target = "createdByDisplayName", source = "ciagInduction.createdBy")
  @Mapping(target = "updatedAt", source = "ciagInduction.modifiedDateTime")
  @Mapping(target = "updatedAtPrison", source = "prisonId")
  @Mapping(target = "updatedBy", source = "ciagInduction.modifiedBy")
  @Mapping(target = "updatedByDisplayName", source = "ciagInduction.modifiedBy")
  fun toWorkOnReleaseMigrationEntity(prisonId: String, ciagInduction: CiagInductionResponse): WorkOnReleaseMigrationEntity

  fun toReasonsNotToWork(domainReasons: List<NotHopingToWorkReason>): Set<ReasonNotToWork>

  fun toAbilityToWorkFactors(domainReasons: List<AffectAbilityToWork>): Set<AbilityToWorkFactor>
}
