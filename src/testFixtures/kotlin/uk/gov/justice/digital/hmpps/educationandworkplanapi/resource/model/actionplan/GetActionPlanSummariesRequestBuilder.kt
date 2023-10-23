package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.anotherValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GetActionPlanSummariesRequest

fun aValidGetActionPlanSummariesRequest(
  prisonNumbers: List<String> = listOf(
    aValidPrisonNumber(),
    anotherValidPrisonNumber(),
  ),
): GetActionPlanSummariesRequest = GetActionPlanSummariesRequest(prisonNumbers = prisonNumbers)
