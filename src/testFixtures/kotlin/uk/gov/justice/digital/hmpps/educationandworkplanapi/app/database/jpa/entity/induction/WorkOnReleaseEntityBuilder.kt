package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import java.util.UUID

fun aValidWorkOnReleaseEntity(
  reference: UUID = UUID.randomUUID(),
  hopingToWork: HopingToWork = HopingToWork.NO,
  notHopingToWorkReasons: List<NotHopingToWorkReason> = listOf(NotHopingToWorkReason.OTHER),
  notHopingToWorkOtherReason: String? = "No motivation",
  affectAbilityToWork: List<AffectAbilityToWork> = listOf(AffectAbilityToWork.OTHER),
  affectAbilityToWorkOther: String? = "Negative attitude",
  createdAtPrison: String? = "BXI",
  updatedAtPrison: String? = "BXI",
) =
  WorkOnReleaseEntity(
    reference = reference,
    hopingToWork = hopingToWork,
    notHopingToWorkReasons = notHopingToWorkReasons,
    notHopingToWorkOtherReason = notHopingToWorkOtherReason,
    affectAbilityToWork = affectAbilityToWork,
    affectAbilityToWorkOther = affectAbilityToWorkOther,
    createdAtPrison = createdAtPrison,
    updatedAtPrison = updatedAtPrison,
  )
