package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.AffectAbilityToWork
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HopingToWork

data class CreateWorkOnReleaseDto(
  val hopingToWork: HopingToWork,
  val affectAbilityToWork: List<AffectAbilityToWork>,
  val affectAbilityToWorkOther: String?,
  val prisonId: String,
)
