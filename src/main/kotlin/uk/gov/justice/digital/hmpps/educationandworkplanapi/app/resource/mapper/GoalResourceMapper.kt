package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalResponse
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
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastUpdatedBy", ignore = true)
  @Mapping(target = "lastUpdatedAt", ignore = true)
  fun fromModelToDomain(createGoalRequest: CreateGoalRequest): Goal

  @Mapping(target = "goalReference", source = "reference")
  // TODO RR-106 - map createdByDisplayName
  @Mapping(target = "createdByDisplayName", constant = "")
  @Mapping(target = "updatedBy", source = "lastUpdatedBy")
  // TODO RR-106 - map updatedByDisplayName
  @Mapping(target = "updatedByDisplayName", constant = "")
  @Mapping(target = "updatedAt", source = "lastUpdatedAt")
  fun fromDomainToModel(goalDomain: Goal): GoalResponse
}
