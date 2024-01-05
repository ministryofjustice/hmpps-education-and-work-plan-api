package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousWorkExperience
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePreviousWorkExperiencesRequest
import java.util.UUID

fun aValidUpdatePreviousWorkExperiencesRequest(
  reference: UUID? = UUID.randomUUID(),
  hasWorkedBefore: Boolean = true,
  experiences: List<PreviousWorkExperience>? = listOf(aValidPreviousWorkExperience()),
): UpdatePreviousWorkExperiencesRequest =
  UpdatePreviousWorkExperiencesRequest(
    reference = reference,
    hasWorkedBefore = hasWorkedBefore,
    experiences = experiences,
  )
