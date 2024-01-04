package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PreviousWorkExperiences
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousWorkExperiencesRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousWorkExperiencesResponse

@Mapper(
  uses = [
    InstantMapper::class,
  ],
  nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
)
interface WorkExperiencesResourceMapper {
  fun toCreatePreviousWorkExperiencesDto(request: CreatePreviousWorkExperiencesRequest, prisonId: String): CreatePreviousWorkExperiencesDto

  @Mapping(target = "updatedBy", source = "lastUpdatedBy")
  @Mapping(target = "updatedByDisplayName", source = "lastUpdatedByDisplayName")
  @Mapping(target = "updatedAt", source = "lastUpdatedAt")
  @Mapping(target = "updatedAtPrison", source = "lastUpdatedAtPrison")
  fun toPreviousWorkExperiencesResponse(workExperiences: PreviousWorkExperiences?): PreviousWorkExperiencesResponse?
}
