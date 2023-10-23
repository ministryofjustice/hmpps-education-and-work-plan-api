package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalResponse
import java.time.LocalDate
import java.util.UUID

fun aValidActionPlanResponse(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234BC",
  reviewDate: LocalDate? = LocalDate.now().plusMonths(6),
  goals: List<GoalResponse> = listOf(aValidGoalResponse(), anotherValidGoalResponse()),
): ActionPlanResponse =
  ActionPlanResponse(
    reference = reference,
    prisonNumber = prisonNumber,
    reviewDate = reviewDate,
    goals = goals,
  )
