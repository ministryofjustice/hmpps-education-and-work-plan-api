package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousQualificationsRequest

@Mapper(nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
interface QualificationsResourceMapper {
  @Mapping(target = "educationLevel", source = "request.educationLevel", defaultValue = "NOT_SURE")
  fun toCreatePreviousQualificationsDto(request: CreatePreviousQualificationsRequest, prisonId: String): CreatePreviousQualificationsDto
}
