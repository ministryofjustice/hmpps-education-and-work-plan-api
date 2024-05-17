package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.AffectAbilityToWork
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HopingToWork
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.NotHopingToWorkReason
import java.util.UUID

data class UpdateWorkOnReleaseDto(
  val reference: UUID?,
  val hopingToWork: HopingToWork,
  val notHopingToWorkReasons: List<NotHopingToWorkReason>,
  val notHopingToWorkOtherReason: String?,
  val affectAbilityToWork: List<AffectAbilityToWork>,
  val affectAbilityToWorkOther: String?,
  val prisonId: String,
)
