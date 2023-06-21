package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.mapper

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanResponse

// TODO - placeholder until MapStruct dependencies are merged in
interface ActionPlanDomainMapper {
  fun fromDomainToModel(actionPlan: ActionPlan): ActionPlanResponse
}
