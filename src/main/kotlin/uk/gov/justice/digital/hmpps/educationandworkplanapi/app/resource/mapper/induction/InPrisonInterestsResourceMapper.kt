package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InPrisonInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.UpdateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateInPrisonInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateInPrisonInterestsRequest

@Mapper(
  uses = [
    InstantMapper::class,
  ],
  nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
)
interface InPrisonInterestsResourceMapper {
  fun toCreateInPrisonInterestsDto(request: CreateInPrisonInterestsRequest, prisonId: String): CreateInPrisonInterestsDto

  @Mapping(target = "updatedBy", source = "lastUpdatedBy")
  @Mapping(target = "updatedByDisplayName", source = "lastUpdatedByDisplayName")
  @Mapping(target = "updatedAt", source = "lastUpdatedAt")
  @Mapping(target = "updatedAtPrison", source = "lastUpdatedAtPrison")
  fun toInPrisonInterestsResponse(inPrisonInterests: InPrisonInterests?): InPrisonInterestsResponse?

  fun toUpdateInPrisonInterestsDto(request: UpdateInPrisonInterestsRequest, prisonId: String): UpdateInPrisonInterestsDto
}
