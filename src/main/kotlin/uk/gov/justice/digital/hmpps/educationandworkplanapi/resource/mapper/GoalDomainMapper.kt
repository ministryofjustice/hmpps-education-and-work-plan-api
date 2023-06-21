package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.mapper

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalRequest

// TODO - placeholder until MapStruct dependencies are merged in
interface GoalDomainMapper {
  fun fromModelToDomain(request: CreateGoalRequest): Goal
}
