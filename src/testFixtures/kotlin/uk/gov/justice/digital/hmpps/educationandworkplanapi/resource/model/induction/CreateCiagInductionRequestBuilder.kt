package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateCiagInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateCiagInductionRequestData

fun aValidCreateCiagInductionRequest(
  requestDTO: CreateCiagInductionRequestData = aValidCreateCiagInductionRequestData(),
  oauth2User: String? = null, // obsolete legacy field from the CIAG team's API
): CreateCiagInductionRequest = CreateCiagInductionRequest(
  requestDTO = requestDTO,
  oauth2User = oauth2User,
)
