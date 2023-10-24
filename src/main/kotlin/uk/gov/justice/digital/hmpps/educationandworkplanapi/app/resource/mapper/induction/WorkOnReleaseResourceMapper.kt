package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateCiagInductionRequest

@Mapper
interface WorkOnReleaseResourceMapper {

  @Mapping(target = "hopingToWork", source = "hopingToGetWork")
  @Mapping(target = "notHopingToWorkReasons", source = "reasonToNotGetWork")
  @Mapping(target = "notHopingToWorkOtherReason", source = "reasonToNotGetWorkOther")
  @Mapping(target = "affectAbilityToWork", source = "abilityToWork")
  @Mapping(target = "affectAbilityToWorkOther", source = "abilityToWorkOther")
  fun toCreateWorkOnReleaseDto(request: CreateCiagInductionRequest): CreateWorkOnReleaseDto
}
