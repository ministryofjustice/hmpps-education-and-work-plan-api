package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousTrainingRequest

@Mapper(nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
interface PreviousTrainingResourceMapper {
  fun toCreatePreviousTrainingDto(request: CreatePreviousTrainingRequest?, prisonId: String): CreatePreviousTrainingDto?
}
