package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.ciag

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.AffectAbilityToWork
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.NotHopingToWorkReason
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AbilityToWorkFactor
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateCiagInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReasonNotToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateCiagInductionRequest

@Mapper(nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
interface CiagWorkOnReleaseResourceMapper {

  @Mapping(target = "hopingToWork", source = "hopingToGetWork")
  @Mapping(target = "notHopingToWorkReasons", source = "reasonToNotGetWork")
  @Mapping(target = "notHopingToWorkOtherReason", source = "reasonToNotGetWorkOther")
  @Mapping(target = "affectAbilityToWork", source = "abilityToWork")
  @Mapping(target = "affectAbilityToWorkOther", source = "abilityToWorkOther")
  fun toCreateWorkOnReleaseDto(request: CreateCiagInductionRequest): CreateWorkOnReleaseDto

  fun toReasonsNotToWork(domainReasons: List<NotHopingToWorkReason>): Set<ReasonNotToWork>

  fun toAbilityToWorkFactors(domainReasons: List<AffectAbilityToWork>): Set<AbilityToWorkFactor>

  @Mapping(target = "hopingToWork", source = "hopingToGetWork")
  @Mapping(target = "notHopingToWorkReasons", source = "reasonToNotGetWork")
  @Mapping(target = "notHopingToWorkOtherReason", source = "reasonToNotGetWorkOther")
  @Mapping(target = "affectAbilityToWork", source = "abilityToWork")
  @Mapping(target = "affectAbilityToWorkOther", source = "abilityToWorkOther")
  fun toUpdateWorkOnReleaseDto(request: UpdateCiagInductionRequest): UpdateWorkOnReleaseDto
}
