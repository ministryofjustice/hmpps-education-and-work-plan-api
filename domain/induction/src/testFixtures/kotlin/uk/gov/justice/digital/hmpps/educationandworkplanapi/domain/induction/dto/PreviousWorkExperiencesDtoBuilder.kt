package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkExperience
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidWorkExperience
import java.util.UUID

fun aValidCreatePreviousWorkExperiencesDto(
  experiences: List<WorkExperience> = listOf(aValidWorkExperience()),
  prisonId: String = "BXI",
) =
  CreatePreviousWorkExperiencesDto(
    experiences = experiences,
    prisonId = prisonId,
  )

fun aValidUpdatePreviousWorkExperiencesDto(
  reference: UUID = UUID.randomUUID(),
  experiences: List<WorkExperience> = listOf(aValidWorkExperience()),
  prisonId: String = "BXI",
) =
  UpdatePreviousWorkExperiencesDto(
    reference = reference,
    experiences = experiences,
    prisonId = prisonId,
  )
