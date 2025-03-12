package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import uk.gov.justice.digital.hmpps.educationandworkplanapi.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GetActionPlanSummariesRequest

fun aValidGetActionPlanSummariesRequest(
  prisonNumbers: List<String> = listOf(
    randomValidPrisonNumber(),
    randomValidPrisonNumber(),
  ),
): GetActionPlanSummariesRequest = GetActionPlanSummariesRequest(prisonNumbers = prisonNumbers)
