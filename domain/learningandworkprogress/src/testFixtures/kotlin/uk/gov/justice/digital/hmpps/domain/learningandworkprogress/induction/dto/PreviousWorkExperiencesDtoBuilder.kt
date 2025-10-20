package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HasWorkedBefore
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkExperience
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidWorkExperience
import java.util.UUID

fun aValidCreatePreviousWorkExperiencesDto(
  hasWorkedBefore: HasWorkedBefore = HasWorkedBefore.YES,
  hasWorkedBeforeNotRelevantReason: String? = null,
  experiences: List<WorkExperience> = listOf(aValidWorkExperience()),
  prisonId: String = "BXI",
) = CreatePreviousWorkExperiencesDto(
  hasWorkedBefore = hasWorkedBefore,
  hasWorkedBeforeNotRelevantReason = hasWorkedBeforeNotRelevantReason,
  experiences = experiences,
  prisonId = prisonId,
)

fun aValidUpdatePreviousWorkExperiencesDto(
  reference: UUID = UUID.randomUUID(),
  hasWorkedBefore: HasWorkedBefore = HasWorkedBefore.YES,
  hasWorkedBeforeNotRelevantReason: String? = null,
  experiences: List<WorkExperience> = listOf(aValidWorkExperience()),
  prisonId: String = "BXI",
) = UpdatePreviousWorkExperiencesDto(
  reference = reference,
  hasWorkedBefore = hasWorkedBefore,
  hasWorkedBeforeNotRelevantReason = hasWorkedBeforeNotRelevantReason,
  experiences = experiences,
  prisonId = prisonId,
)
