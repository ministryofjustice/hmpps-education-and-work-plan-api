package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateInPrisonInterestsRequest

@Mapper(nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
abstract class InPrisonInterestsResourceMapper {
  fun toCreateInPrisonInterestsDto(
    request: CreateInPrisonInterestsRequest?,
    prisonId: String,
  ): CreateInPrisonInterestsDto? {
    return if (request == null) {
      null
    } else {
      convertToCreateInPrisonInterestsDto(request, prisonId)
    }
  }

  abstract fun convertToCreateInPrisonInterestsDto(
    request: CreateInPrisonInterestsRequest?,
    prisonId: String,
  ): CreateInPrisonInterestsDto?
}
