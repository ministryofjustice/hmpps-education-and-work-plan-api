package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity

import java.time.Instant
import java.util.UUID

fun aValidWorkOnReleaseMigrationEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  hopingToWork: HopingToWork = HopingToWork.NO,
  notHopingToWorkReasons: MutableList<NotHopingToWorkReason> = mutableListOf(NotHopingToWorkReason.OTHER),
  notHopingToWorkOtherReason: String? = "No motivation",
  affectAbilityToWork: MutableList<AffectAbilityToWork> = mutableListOf(AffectAbilityToWork.OTHER),
  affectAbilityToWorkOther: String? = "Negative attitude",
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  updatedAt: Instant? = Instant.now(),
  updatedAtPrison: String = "BXI",
  updatedBy: String? = "bjones_gen",
  updatedByDisplayName: String? = "Barry Jones",
) =
  WorkOnReleaseMigrationEntity(
    id = id,
    reference = reference,
    hopingToWork = hopingToWork,
    notHopingToWorkReasons = notHopingToWorkReasons,
    notHopingToWorkOtherReason = notHopingToWorkOtherReason,
    affectAbilityToWork = affectAbilityToWork,
    affectAbilityToWorkOther = affectAbilityToWorkOther,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    updatedAt = updatedAt,
    updatedAtPrison = updatedAtPrison,
    updatedBy = updatedBy,
    updatedByDisplayName = updatedByDisplayName,
  )
