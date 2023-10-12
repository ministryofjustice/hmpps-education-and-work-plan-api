package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

import java.util.UUID

fun aValidInduction(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234AB",
) = Induction(
  reference = reference,
  prisonNumber = prisonNumber,
)
