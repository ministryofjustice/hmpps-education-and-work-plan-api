package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PersonalSkillsAndInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.UpdatePersonalSkillsAndInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePersonalSkillsAndInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkillsAndInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePersonalSkillsAndInterestsRequest

@Mapper(
  uses = [
    InstantMapper::class,
  ],
  nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
)
interface SkillsAndInterestsResourceMapper {
  fun toCreatePersonalSkillsAndInterestsDto(request: CreatePersonalSkillsAndInterestsRequest, prisonId: String): CreatePersonalSkillsAndInterestsDto

  @Mapping(target = "updatedBy", source = "lastUpdatedBy")
  @Mapping(target = "updatedByDisplayName", source = "lastUpdatedByDisplayName")
  @Mapping(target = "updatedAt", source = "lastUpdatedAt")
  @Mapping(target = "updatedAtPrison", source = "lastUpdatedAtPrison")
  fun toPersonalSkillsAndInterestsResponse(personalSkillsAndInterests: PersonalSkillsAndInterests?): PersonalSkillsAndInterestsResponse?

  fun toUpdatePersonalSkillsAndInterestsDto(request: UpdatePersonalSkillsAndInterestsRequest, prisonId: String): UpdatePersonalSkillsAndInterestsDto
}
