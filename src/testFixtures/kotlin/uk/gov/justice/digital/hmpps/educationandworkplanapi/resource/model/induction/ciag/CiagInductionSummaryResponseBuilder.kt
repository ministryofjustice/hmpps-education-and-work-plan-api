package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CiagInductionSummaryResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork
import java.time.OffsetDateTime

fun aValidCiagInductionSummaryResponse(
  offenderId: String = aValidPrisonNumber(),
  hopingToGetWork: HopingToWork = HopingToWork.NOT_SURE,
  desireToWork: Boolean = false,
  createdBy: String = "bjones_gen",
  createdDateTime: OffsetDateTime = OffsetDateTime.now(),
  modifiedBy: String = "bjones_gen",
  modifiedDateTime: OffsetDateTime = OffsetDateTime.now(),
): CiagInductionSummaryResponse =
  CiagInductionSummaryResponse(
    offenderId = offenderId,
    desireToWork = desireToWork,
    hopingToGetWork = hopingToGetWork,
    createdBy = createdBy,
    createdDateTime = createdDateTime,
    modifiedBy = modifiedBy,
    modifiedDateTime = modifiedDateTime,
  )
