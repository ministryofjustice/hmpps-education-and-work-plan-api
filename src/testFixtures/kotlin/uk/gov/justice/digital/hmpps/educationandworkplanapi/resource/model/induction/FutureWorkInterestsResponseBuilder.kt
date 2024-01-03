package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.FutureWorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.FutureWorkInterestsResponse
import java.time.OffsetDateTime
import java.util.UUID

fun aValidFutureWorkInterestsResponse(
  reference: UUID = UUID.randomUUID(),
  interests: List<FutureWorkInterest> = listOf(aValidFutureWorkInterest()),
  createdBy: String = "asmith_gen",
  createdByDisplayName: String = "Alex Smith",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdAtPrison: String = "BXI",
  updatedBy: String = "asmith_gen",
  updatedByDisplayName: String = "Alex Smith",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
  updatedAtPrison: String = "BXI",
): FutureWorkInterestsResponse =
  FutureWorkInterestsResponse(
    reference = reference,
    interests = interests,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    updatedBy = updatedBy,
    updatedByDisplayName = updatedByDisplayName,
    updatedAt = updatedAt,
    updatedAtPrison = updatedAtPrison,
  )
