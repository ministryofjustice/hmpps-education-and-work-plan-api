package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateWorkOnReleaseRequest

@Mapper(nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
interface WorkOnReleaseResourceMapper {
  fun toCreateWorkOnReleaseDto(request: CreateWorkOnReleaseRequest, prisonId: String): CreateWorkOnReleaseDto
}
