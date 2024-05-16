package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.domain.induction.PreviousQualifications
import uk.gov.justice.digital.hmpps.domain.induction.dto.CreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.induction.dto.UpdatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousQualificationsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePreviousQualificationsRequest

@Mapper(
  uses = [
    InstantMapper::class,
  ],
  nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
)
interface QualificationsResourceMapper {
  @Mapping(target = "educationLevel", source = "request.educationLevel", defaultValue = "NOT_SURE")
  fun toCreatePreviousQualificationsDto(request: CreatePreviousQualificationsRequest, prisonId: String): CreatePreviousQualificationsDto

  @Mapping(target = "updatedBy", source = "lastUpdatedBy")
  @Mapping(target = "updatedByDisplayName", source = "lastUpdatedByDisplayName")
  @Mapping(target = "updatedAt", source = "lastUpdatedAt")
  @Mapping(target = "updatedAtPrison", source = "lastUpdatedAtPrison")
  fun toPreviousQualificationsResponse(previousQualifications: PreviousQualifications?): PreviousQualificationsResponse?

  fun toUpdatePreviousQualificationsDto(request: UpdatePreviousQualificationsRequest, prisonId: String): UpdatePreviousQualificationsDto
}
