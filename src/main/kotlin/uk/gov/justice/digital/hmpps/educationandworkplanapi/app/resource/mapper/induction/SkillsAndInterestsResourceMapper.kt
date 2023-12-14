package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePersonalSkillsAndInterestsRequest

@Mapper(nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
abstract class SkillsAndInterestsResourceMapper {
  fun toCreatePersonalSkillsAndInterestsDto(
    request: CreatePersonalSkillsAndInterestsRequest?,
    prisonId: String,
  ): CreatePersonalSkillsAndInterestsDto? {
    return if (request == null) {
      null
    } else {
      convertToCreatePersonalSkillsAndInterestsDto(request, prisonId)
    }
  }

  abstract fun convertToCreatePersonalSkillsAndInterestsDto(
    request: CreatePersonalSkillsAndInterestsRequest?,
    prisonId: String,
  ): CreatePersonalSkillsAndInterestsDto?
}
