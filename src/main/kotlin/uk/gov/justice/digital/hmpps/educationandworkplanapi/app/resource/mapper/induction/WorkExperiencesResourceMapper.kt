package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousWorkExperiencesRequest

@Mapper(nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
abstract class WorkExperiencesResourceMapper {
  fun toCreatePreviousWorkExperiencesDto(
    request: CreatePreviousWorkExperiencesRequest?,
    prisonId: String,
  ): CreatePreviousWorkExperiencesDto? {
    return if (request == null) {
      null
    } else {
      convertToCreatePreviousWorkExperiencesDto(request, prisonId)
    }
  }

  abstract fun convertToCreatePreviousWorkExperiencesDto(
    request: CreatePreviousWorkExperiencesRequest?,
    prisonId: String,
  ): CreatePreviousWorkExperiencesDto?
}
