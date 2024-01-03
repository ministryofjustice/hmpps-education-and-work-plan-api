package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonTrainingInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonWorkInterest
import java.time.OffsetDateTime
import java.util.UUID

fun aValidInPrisonInterestsResponse(
  reference: UUID = UUID.randomUUID(),
  inPrisonWorkInterests: List<InPrisonWorkInterest> = listOf(aValidInPrisonWorkInterest()),
  inPrisonTrainingInterests: List<InPrisonTrainingInterest> = listOf(aValidInPrisonTrainingInterest()),
  createdBy: String = "asmith_gen",
  createdByDisplayName: String = "Alex Smith",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdAtPrison: String = "BXI",
  updatedBy: String = "asmith_gen",
  updatedByDisplayName: String = "Alex Smith",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
  updatedAtPrison: String = "BXI",
): InPrisonInterestsResponse =
  InPrisonInterestsResponse(
    reference = reference,
    inPrisonWorkInterests = inPrisonWorkInterests,
    inPrisonTrainingInterests = inPrisonTrainingInterests,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    updatedBy = updatedBy,
    updatedByDisplayName = updatedByDisplayName,
    updatedAt = updatedAt,
    updatedAtPrison = updatedAtPrison,
  )
