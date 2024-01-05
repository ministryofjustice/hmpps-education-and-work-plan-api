package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.FutureWorkInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.UpdateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateFutureWorkInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.FutureWorkInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateFutureWorkInterestsRequest

@Mapper(
  uses = [
    InstantMapper::class,
  ],
  nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
)
interface WorkInterestsResourceMapper {
  fun toCreateFutureWorkInterestsDto(request: CreateFutureWorkInterestsRequest, prisonId: String): CreateFutureWorkInterestsDto

  @Mapping(target = "updatedBy", source = "lastUpdatedBy")
  @Mapping(target = "updatedByDisplayName", source = "lastUpdatedByDisplayName")
  @Mapping(target = "updatedAt", source = "lastUpdatedAt")
  @Mapping(target = "updatedAtPrison", source = "lastUpdatedAtPrison")
  fun toFutureWorkInterestsResponse(futureWorkInterests: FutureWorkInterests?): FutureWorkInterestsResponse?

  fun toUpdateFutureWorkInterestsDto(request: UpdateFutureWorkInterestsRequest, prisonId: String): UpdateFutureWorkInterestsDto
}
