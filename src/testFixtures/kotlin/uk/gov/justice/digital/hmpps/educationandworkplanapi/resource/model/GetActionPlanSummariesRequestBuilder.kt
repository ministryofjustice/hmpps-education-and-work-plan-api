package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.anotherValidPrisonNumber

fun aValidGetActionPlanSummariesRequest(
  prisonNumbers: List<String> = listOf(
    aValidPrisonNumber(),
    anotherValidPrisonNumber(),
  ),
): GetActionPlanSummariesRequest = GetActionPlanSummariesRequest(prisonNumbers = prisonNumbers)
