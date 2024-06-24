package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HasWorkedBefore
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousWorkExperience
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePreviousWorkExperiencesRequest
import java.util.UUID

fun aValidUpdatePreviousWorkExperiencesRequest(
  reference: UUID? = UUID.randomUUID(),
  hasWorkedBefore: HasWorkedBefore = HasWorkedBefore.YES,
  hasWorkedBeforeNotRelevantReason: String? = null,
  experiences: List<PreviousWorkExperience>? = listOf(aValidPreviousWorkExperience()),
): UpdatePreviousWorkExperiencesRequest =
  UpdatePreviousWorkExperiencesRequest(
    reference = reference,
    hasWorkedBefore = hasWorkedBefore,
    hasWorkedBeforeNotRelevantReason = hasWorkedBeforeNotRelevantReason,
    experiences = experiences,
  )
