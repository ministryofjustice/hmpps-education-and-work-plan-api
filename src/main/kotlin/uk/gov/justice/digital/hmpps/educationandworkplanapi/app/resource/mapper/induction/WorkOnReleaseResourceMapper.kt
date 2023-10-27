package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.AffectAbilityToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.NotHopingToWorkReason
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AbilityToWorkFactor
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateCiagInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReasonNotToWork

@Mapper
interface WorkOnReleaseResourceMapper {

  @Mapping(target = "hopingToWork", source = "hopingToGetWork")
  @Mapping(target = "notHopingToWorkReasons", source = "reasonToNotGetWork")
  @Mapping(target = "notHopingToWorkOtherReason", source = "reasonToNotGetWorkOther")
  @Mapping(target = "affectAbilityToWork", source = "abilityToWork")
  @Mapping(target = "affectAbilityToWorkOther", source = "abilityToWorkOther")
  fun toCreateWorkOnReleaseDto(request: CreateCiagInductionRequest): CreateWorkOnReleaseDto

  fun toReasonsNotToWork(domainReasons: List<NotHopingToWorkReason>): Set<ReasonNotToWork>

  fun toAbilityToWorkFactors(domainReasons: List<AffectAbilityToWork>): Set<AbilityToWorkFactor>
}
