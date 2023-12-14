package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateFutureWorkInterestsRequest

@Mapper(nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
interface WorkInterestsResourceMapper {
  fun toCreateFutureWorkInterestsDto(request: CreateFutureWorkInterestsRequest?, prisonId: String): CreateFutureWorkInterestsDto?
}
