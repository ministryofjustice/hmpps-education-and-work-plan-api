package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.domain.induction.PreviousTraining
import uk.gov.justice.digital.hmpps.domain.induction.dto.CreatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.domain.induction.dto.UpdatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousTrainingRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousTrainingResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePreviousTrainingRequest

@Mapper(
  uses = [
    InstantMapper::class,
  ],
  nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
)
interface PreviousTrainingResourceMapper {
  fun toCreatePreviousTrainingDto(request: CreatePreviousTrainingRequest, prisonId: String): CreatePreviousTrainingDto

  @Mapping(target = "updatedBy", source = "lastUpdatedBy")
  @Mapping(target = "updatedByDisplayName", source = "lastUpdatedByDisplayName")
  @Mapping(target = "updatedAt", source = "lastUpdatedAt")
  @Mapping(target = "updatedAtPrison", source = "lastUpdatedAtPrison")
  fun toPreviousTrainingResponse(previousTraining: PreviousTraining): PreviousTrainingResponse?

  fun toUpdatePreviousTrainingDto(request: UpdatePreviousTrainingRequest, prisonId: String): UpdatePreviousTrainingDto
}
