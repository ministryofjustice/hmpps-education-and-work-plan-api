package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.anotherValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GetCiagInductionSummariesRequest

fun aValidGetCiagInductionSummariesRequest(
  offenderIds: List<String> = listOf(
    aValidPrisonNumber(),
    anotherValidPrisonNumber(),
  ),
): GetCiagInductionSummariesRequest = GetCiagInductionSummariesRequest(offenderIds = offenderIds)
