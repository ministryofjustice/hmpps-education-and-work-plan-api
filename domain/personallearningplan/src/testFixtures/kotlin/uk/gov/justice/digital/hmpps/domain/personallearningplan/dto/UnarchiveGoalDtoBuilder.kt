package uk.gov.justice.digital.hmpps.domain.personallearningplan.dto

import java.util.*

fun aValidUnarchiveGoalDto(
  reference: UUID = UUID.randomUUID(),
  prisonId: String = "BXI",
): UnarchiveGoalDto =
  UnarchiveGoalDto(
    reference = reference,
    prisonId = prisonId,
  )
