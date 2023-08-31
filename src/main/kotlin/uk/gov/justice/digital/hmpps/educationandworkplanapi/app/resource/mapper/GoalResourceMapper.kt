package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
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
  @Mapping(target = "reference", expression = "java(UUID.randomUUID())")
  @Mapping(target = "status", constant = "ACTIVE")
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "createdByDisplayName", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastUpdatedBy", ignore = true)
  @Mapping(target = "lastUpdatedByDisplayName", ignore = true)
  @Mapping(target = "lastUpdatedAt", ignore = true)
  fun fromModelToDomain(createGoalRequest: CreateGoalRequest): Goal

  @Mapping(target = "reference", source = "goalReference")
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "createdByDisplayName", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastUpdatedBy", ignore = true)
  @Mapping(target = "lastUpdatedByDisplayName", ignore = true)
  @Mapping(target = "lastUpdatedAt", ignore = true)
  fun fromModelToDomain(updateGoalRequest: UpdateGoalRequest): Goal

  @Mapping(target = "goalReference", source = "reference")
  @Mapping(target = "updatedBy", source = "lastUpdatedBy")
  @Mapping(target = "updatedByDisplayName", source = "lastUpdatedByDisplayName")
  @Mapping(target = "updatedAt", source = "lastUpdatedAt")
  @Mapping(target = "createdAtPrison", constant = "BXI") // TODO RR-236 map from the domain object
  @Mapping(target = "updatedAtPrison", constant = "BXI") // TODO RR-236 map from the domain object
  fun fromDomainToModel(goalDomain: Goal): GoalResponse
}
