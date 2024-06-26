package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction

import java.time.Instant
import java.util.UUID

fun aValidWorkOnRelease(
  reference: UUID = UUID.randomUUID(),
  hopingToWork: HopingToWork = HopingToWork.YES,
  affectAbilityToWork: List<AffectAbilityToWork> = emptyList(),
  affectAbilityToWorkOther: String? = null,
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  lastUpdatedBy: String? = "bjones_gen",
  lastUpdatedByDisplayName: String? = "Barry Jones",
  lastUpdatedAt: Instant? = Instant.now(),
  lastUpdatedAtPrison: String = "BXI",
) = WorkOnRelease(
  reference = reference,
  hopingToWork = hopingToWork,
  affectAbilityToWork = affectAbilityToWork,
  affectAbilityToWorkOther = affectAbilityToWorkOther,
  createdBy = createdBy,
  createdByDisplayName = createdByDisplayName,
  createdAt = createdAt,
  createdAtPrison = createdAtPrison,
  lastUpdatedBy = lastUpdatedBy,
  lastUpdatedByDisplayName = lastUpdatedByDisplayName,
  lastUpdatedAt = lastUpdatedAt,
  lastUpdatedAtPrison = lastUpdatedAtPrison,
)
