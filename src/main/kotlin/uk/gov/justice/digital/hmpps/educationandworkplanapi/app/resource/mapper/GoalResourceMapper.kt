package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.CreateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.UpdateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateGoalRequest
import java.time.Instant
import java.util.UUID

@Mapper(
  uses = [
    StepResourceMapper::class,
    InstantMapper::class,
  ],
  imports = [
    Instant::class,
    UUID::class,
  ],
)
interface GoalResourceMapper {
  @Mapping(target = "status", constant = "ACTIVE")
  fun fromModelToDto(createGoalRequest: CreateGoalRequest): CreateGoalDto

  @Mapping(target = "reference", source = "goalReference")
  fun fromModelToDto(updateGoalRequest: UpdateGoalRequest): UpdateGoalDto

  @Mapping(target = "goalReference", source = "reference")
  @Mapping(target = "updatedBy", source = "lastUpdatedBy")
  @Mapping(target = "updatedByDisplayName", source = "lastUpdatedByDisplayName")
  @Mapping(target = "updatedAt", source = "lastUpdatedAt")
  @Mapping(target = "createdAtPrison", source = "createdAtPrison")
  @Mapping(target = "updatedAtPrison", source = "lastUpdatedAtPrison")
  fun fromDomainToModel(goalDomain: Goal): GoalResponse
}
