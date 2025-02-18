package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AffectAbilityToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkOnReleaseResponse
import java.time.OffsetDateTime
import java.util.UUID

fun aValidWorkOnReleaseResponse(): WorkOnReleaseResponse = aValidWorkOnReleaseResponseForPrisonerNotLookingToWork()

fun aValidWorkOnReleaseResponseForPrisonerNotLookingToWork(
  reference: UUID = UUID.randomUUID(),
  hopingToWork: HopingToWork = HopingToWork.NO,
  affectAbilityToWork: List<AffectAbilityToWork> = listOf(AffectAbilityToWork.OTHER),
  affectAbilityToWorkOther: String? = "Employers aren't interested",
  createdBy: String = "asmith_gen",
  createdByDisplayName: String = "Alex Smith",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdAtPrison: String = "BXI",
  updatedBy: String = "asmith_gen",
  updatedByDisplayName: String = "Alex Smith",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
  updatedAtPrison: String = "BXI",
): WorkOnReleaseResponse = WorkOnReleaseResponse(
  reference = reference,
  hopingToWork = hopingToWork,
  affectAbilityToWork = affectAbilityToWork,
  affectAbilityToWorkOther = affectAbilityToWorkOther,
  createdBy = createdBy,
  createdByDisplayName = createdByDisplayName,
  createdAt = createdAt,
  createdAtPrison = createdAtPrison,
  updatedBy = updatedBy,
  updatedByDisplayName = updatedByDisplayName,
  updatedAt = updatedAt,
  updatedAtPrison = updatedAtPrison,
)

fun aValidWorkOnReleaseResponseForPrisonerLookingToWork(
  reference: UUID = UUID.randomUUID(),
  hopingToWork: HopingToWork = HopingToWork.YES,
  affectAbilityToWork: List<AffectAbilityToWork> = listOf(AffectAbilityToWork.NONE),
  affectAbilityToWorkOther: String? = null,
  createdBy: String = "asmith_gen",
  createdByDisplayName: String = "Alex Smith",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdAtPrison: String = "BXI",
  updatedBy: String = "asmith_gen",
  updatedByDisplayName: String = "Alex Smith",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
  updatedAtPrison: String = "BXI",
): WorkOnReleaseResponse = WorkOnReleaseResponse(
  reference = reference,
  hopingToWork = hopingToWork,
  affectAbilityToWork = affectAbilityToWork,
  affectAbilityToWorkOther = affectAbilityToWorkOther,
  createdBy = createdBy,
  createdByDisplayName = createdByDisplayName,
  createdAt = createdAt,
  createdAtPrison = createdAtPrison,
  updatedBy = updatedBy,
  updatedByDisplayName = updatedByDisplayName,
  updatedAt = updatedAt,
  updatedAtPrison = updatedAtPrison,
)
