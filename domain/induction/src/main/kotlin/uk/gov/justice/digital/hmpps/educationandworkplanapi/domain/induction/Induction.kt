package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

import java.util.UUID

/**
 * Represents a Prisoner's Induction, which is typically carried out by a CIAG officer shortly after the Prisoner
 * has entered a Prison (either when starting a new sentence, or after being transferred from another Prison). Once
 * established, an Induction can be reviewed and updated periodically, depending on the practices of the Prison and
 * the needs of the Prisoner.
 */
// TODO - RR-421 - add required fields
class Induction(
  val reference: UUID,
  val prisonNumber: String,
)
