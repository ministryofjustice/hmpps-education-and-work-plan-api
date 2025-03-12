package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag

import uk.gov.justice.digital.hmpps.educationandworkplanapi.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GetCiagInductionSummariesRequest

fun aValidGetCiagInductionSummariesRequest(
  offenderIds: List<String> = listOf(
    randomValidPrisonNumber(),
    randomValidPrisonNumber(),
  ),
): GetCiagInductionSummariesRequest = GetCiagInductionSummariesRequest(offenderIds = offenderIds)
