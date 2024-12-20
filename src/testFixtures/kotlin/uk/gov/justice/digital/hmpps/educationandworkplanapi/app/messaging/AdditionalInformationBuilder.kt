package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation

fun aValidPrisonerReceivedAdditionalInformation(
  prisonNumber: String = aValidPrisonNumber(),
  prisonId: String = "BXI",
  reason: PrisonerReceivedAdditionalInformation.Reason = PrisonerReceivedAdditionalInformation.Reason.ADMISSION,
  currentLocation: PrisonerReceivedAdditionalInformation.Location = PrisonerReceivedAdditionalInformation.Location.IN_PRISON,
  currentPrisonStatus: PrisonerReceivedAdditionalInformation.PrisonStatus = PrisonerReceivedAdditionalInformation.PrisonStatus.UNDER_PRISON_CARE,
): PrisonerReceivedAdditionalInformation =
  PrisonerReceivedAdditionalInformation(
    nomsNumber = prisonNumber,
    reason = reason,
    details = "ACTIVE IN:ADM-N",
    currentLocation = currentLocation,
    prisonId = prisonId,
    nomisMovementReasonCode = "N",
    currentPrisonStatus = currentPrisonStatus,
  )

fun aValidPrisonerReleasedAdditionalInformation(
  prisonNumber: String = aValidPrisonNumber(),
  prisonId: String = "BXI",
  reason: PrisonerReleasedAdditionalInformation.Reason = PrisonerReleasedAdditionalInformation.Reason.RELEASED,
  currentLocation: PrisonerReleasedAdditionalInformation.Location = PrisonerReleasedAdditionalInformation.Location.OUTSIDE_PRISON,
  currentPrisonStatus: PrisonerReleasedAdditionalInformation.PrisonStatus = PrisonerReleasedAdditionalInformation.PrisonStatus.NOT_UNDER_PRISON_CARE,
): PrisonerReleasedAdditionalInformation =
  PrisonerReleasedAdditionalInformation(
    nomsNumber = prisonNumber,
    reason = reason,
    details = "ACTIVE IN:ADM-N",
    currentLocation = currentLocation,
    prisonId = prisonId,
    nomisMovementReasonCode = "N",
    currentPrisonStatus = currentPrisonStatus,
  )
