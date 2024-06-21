package uk.gov.justice.digital.hmpps.domain.personallearningplan.dto

import java.util.*

fun aValidUnarchiveGoalDto(
  reference: UUID = UUID.randomUUID(),
): UnarchiveGoalDto =
  UnarchiveGoalDto(
    reference = reference,
  )
