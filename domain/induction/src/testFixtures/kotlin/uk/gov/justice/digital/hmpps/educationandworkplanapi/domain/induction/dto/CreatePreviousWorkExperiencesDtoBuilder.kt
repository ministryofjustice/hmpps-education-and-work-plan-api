package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkExperience
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidWorkExperience

fun aValidCreatePreviousWorkExperiencesDto(
  experiences: List<WorkExperience> = listOf(aValidWorkExperience()),
  prisonId: String = "BXI",
) =
  CreatePreviousWorkExperiencesDto(
    experiences = experiences,
    prisonId = prisonId,
  )
