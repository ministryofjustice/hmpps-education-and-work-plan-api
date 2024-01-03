package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePrisonWorkAndEducationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonTrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonWorkType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePrisonWorkAndEducationRequest
import java.util.UUID

fun aValidCreatePrisonWorkAndEducationRequest(
  inPrisonWork: Set<InPrisonWorkType>? = setOf(InPrisonWorkType.OTHER),
  inPrisonWorkOther: String? = "Any in-prison work",
  inPrisonEducation: Set<InPrisonTrainingType>? = setOf(InPrisonTrainingType.OTHER),
  inPrisonEducationOther: String? = "Any in-prison training",
): CreatePrisonWorkAndEducationRequest =
  CreatePrisonWorkAndEducationRequest(
    inPrisonWork = inPrisonWork,
    inPrisonWorkOther = inPrisonWorkOther,
    inPrisonEducation = inPrisonEducation,
    inPrisonEducationOther = inPrisonEducationOther,
  )

fun aValidUpdatePrisonWorkAndEducationRequest(
  id: UUID? = UUID.randomUUID(),
  inPrisonWork: Set<InPrisonWorkType>? = setOf(InPrisonWorkType.OTHER),
  inPrisonWorkOther: String? = "Any in-prison work",
  inPrisonEducation: Set<InPrisonTrainingType>? = setOf(InPrisonTrainingType.OTHER),
  inPrisonEducationOther: String? = "Any in-prison training",
): UpdatePrisonWorkAndEducationRequest =
  UpdatePrisonWorkAndEducationRequest(
    id = id,
    inPrisonWork = inPrisonWork,
    inPrisonWorkOther = inPrisonWorkOther,
    inPrisonEducation = inPrisonEducation,
    inPrisonEducationOther = inPrisonEducationOther,
  )
