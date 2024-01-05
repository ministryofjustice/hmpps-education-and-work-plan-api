package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkOnRelease
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.UpdateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateWorkOnReleaseRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateWorkOnReleaseRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkOnReleaseResponse

@Mapper(
  uses = [
    InstantMapper::class,
  ],
  nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
)
interface WorkOnReleaseResourceMapper {
  fun toCreateWorkOnReleaseDto(request: CreateWorkOnReleaseRequest, prisonId: String): CreateWorkOnReleaseDto

  @Mapping(target = "updatedBy", source = "lastUpdatedBy")
  @Mapping(target = "updatedByDisplayName", source = "lastUpdatedByDisplayName")
  @Mapping(target = "updatedAt", source = "lastUpdatedAt")
  @Mapping(target = "updatedAtPrison", source = "lastUpdatedAtPrison")
  fun toWorkOnReleaseResponse(workOnRelease: WorkOnRelease): WorkOnReleaseResponse

  fun toUpdateWorkOnReleaseDto(request: UpdateWorkOnReleaseRequest, prisonId: String): UpdateWorkOnReleaseDto
}
