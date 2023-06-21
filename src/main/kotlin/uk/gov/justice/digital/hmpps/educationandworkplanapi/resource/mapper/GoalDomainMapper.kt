package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalResponse
import java.time.Instant
import java.util.UUID

@Mapper(
  uses = [
    StepDomainMapper::class,
    InstantMapper::class,
  ],
  imports = [
    Instant::class,
    UUID::class,
  ],
)
interface GoalDomainMapper {
  @Mapping(target = "reference", expression = "java(UUID.randomUUID())")
  @Mapping(target = "status", constant = "ACTIVE")
  fun fromModelToDomain(createGoalRequest: CreateGoalRequest): Goal

  @Mapping(target = "goalReference", source = "reference")
  @Mapping(target = "updatedBy", source = "lastUpdatedBy")
  @Mapping(target = "updatedAt", source = "lastUpdatedAt")
  fun fromDomainToModel(goalDomain: Goal): GoalResponse
}
