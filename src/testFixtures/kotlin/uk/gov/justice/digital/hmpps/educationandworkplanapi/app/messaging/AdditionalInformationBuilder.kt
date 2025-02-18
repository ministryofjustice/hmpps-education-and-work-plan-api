package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerMergedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerMergedAdditionalInformation.Reason
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation

fun aValidPrisonerReceivedAdditionalInformation(
  prisonNumber: String = aValidPrisonNumber(),
  prisonId: String = "BXI",
  reason: PrisonerReceivedAdditionalInformation.Reason = PrisonerReceivedAdditionalInformation.Reason.ADMISSION,
  details: String? = "ACTIVE IN:ADM-N",
  currentLocation: PrisonerReceivedAdditionalInformation.Location = PrisonerReceivedAdditionalInformation.Location.IN_PRISON,
  currentPrisonStatus: PrisonerReceivedAdditionalInformation.PrisonStatus = PrisonerReceivedAdditionalInformation.PrisonStatus.UNDER_PRISON_CARE,
  nomisMovementReasonCode: String = "N",
): PrisonerReceivedAdditionalInformation = PrisonerReceivedAdditionalInformation(
  nomsNumber = prisonNumber,
  reason = reason,
  details = details,
  currentLocation = currentLocation,
  prisonId = prisonId,
  nomisMovementReasonCode = nomisMovementReasonCode,
  currentPrisonStatus = currentPrisonStatus,
)

fun aValidPrisonerReleasedAdditionalInformation(
  prisonNumber: String = aValidPrisonNumber(),
  prisonId: String = "BXI",
  reason: PrisonerReleasedAdditionalInformation.Reason = PrisonerReleasedAdditionalInformation.Reason.RELEASED,
  details: String? = "Movement reason code CR",
  currentLocation: PrisonerReleasedAdditionalInformation.Location = PrisonerReleasedAdditionalInformation.Location.OUTSIDE_PRISON,
  currentPrisonStatus: PrisonerReleasedAdditionalInformation.PrisonStatus = PrisonerReleasedAdditionalInformation.PrisonStatus.NOT_UNDER_PRISON_CARE,
  nomisMovementReasonCode: String = "CR",
): PrisonerReleasedAdditionalInformation = PrisonerReleasedAdditionalInformation(
  nomsNumber = prisonNumber,
  reason = reason,
  details = details,
  currentLocation = currentLocation,
  prisonId = prisonId,
  nomisMovementReasonCode = nomisMovementReasonCode,
  currentPrisonStatus = currentPrisonStatus,
)

fun aValidPrisonerMergedAdditionalInformation(
  prisonNumber: String = aValidPrisonNumber(),
  removedNomsNumber: String,
  reason: Reason = Reason.MERGE,
): PrisonerMergedAdditionalInformation = PrisonerMergedAdditionalInformation(
  nomsNumber = prisonNumber,
  reason = reason,
  removedNomsNumber = removedNomsNumber,
)
