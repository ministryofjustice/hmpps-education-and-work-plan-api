package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.AffectAbilityToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.HopingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.NotHopingToWorkReason

data class CreateWorkOnReleaseDto(
  val hopingToWork: HopingToWork,
  val notHopingToWorkReasons: List<NotHopingToWorkReason>,
  val notHopingToWorkOtherReason: String?,
  val affectAbilityToWork: List<AffectAbilityToWork>,
  val affectAbilityToWorkOther: String?,
  val prisonId: String,
)
