package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.ValueMapping
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateCiagInductionRequestData
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.HopingToWork as HopingToWorkDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork as HopingToWorkApi

@Mapper
interface WorkOnReleaseResourceMapper {

  @Mapping(target = "hopingToWork", source = "hopingToGetWork")
  @Mapping(target = "notHopingToWorkReasons", source = "reasonToNotGetWork")
  @Mapping(target = "notHopingToWorkOtherReason", source = "reasonToNotGetWorkOther")
  @Mapping(target = "affectAbilityToWork", source = "abilityToWork")
  @Mapping(target = "affectAbilityToWorkOther", source = "abilityToWorkOther")
  fun toCreateWorkOnReleaseDto(request: CreateCiagInductionRequestData): CreateWorkOnReleaseDto

  @ValueMapping(target = "YES", source = "TRUE")
  @ValueMapping(target = "NO", source = "FALSE")
  @ValueMapping(target = "NOT_SURE", source = "NOT_SURE")
  fun toHopingToWork(hopingToWork: HopingToWorkApi): HopingToWorkDomain
}
