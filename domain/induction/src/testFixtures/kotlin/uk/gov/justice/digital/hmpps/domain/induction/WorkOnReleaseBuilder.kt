package uk.gov.justice.digital.hmpps.domain.induction

import java.time.Instant
import java.util.UUID

fun aValidWorkOnRelease(
  reference: UUID = UUID.randomUUID(),
  hopingToWork: HopingToWork = HopingToWork.YES,
  notHopingToWorkReasons: List<NotHopingToWorkReason> = emptyList(),
  notHopingToWorkOtherReason: String? = null,
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
  notHopingToWorkReasons = notHopingToWorkReasons,
  notHopingToWorkOtherReason = notHopingToWorkOtherReason,
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
