package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import uk.gov.justice.digital.hmpps.educationandworkplanapi.anotherValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GetActionPlanSummariesRequest

fun aValidGetActionPlanSummariesRequest(
  prisonNumbers: List<String> = listOf(
    randomValidPrisonNumber(),
    anotherValidPrisonNumber(),
  ),
): GetActionPlanSummariesRequest = GetActionPlanSummariesRequest(prisonNumbers = prisonNumbers)
