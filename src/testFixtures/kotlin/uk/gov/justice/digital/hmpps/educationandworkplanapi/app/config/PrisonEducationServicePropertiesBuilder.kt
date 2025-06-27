package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import java.time.LocalDate

fun aPrisonEducationServiceProperties() = PrisonEducationServiceProperties(
  contractStartDate = LocalDate.of(2025, 10, 1),
)
