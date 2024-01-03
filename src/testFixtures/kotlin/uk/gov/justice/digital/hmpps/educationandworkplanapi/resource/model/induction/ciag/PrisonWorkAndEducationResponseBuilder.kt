package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonTrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonWorkType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PrisonWorkAndEducationResponse
import java.time.OffsetDateTime
import java.util.UUID

fun aValidPrisonWorkAndEducationResponse(
  id: UUID = UUID.randomUUID(),
  inPrisonWork: Set<InPrisonWorkType>? = setOf(InPrisonWorkType.OTHER),
  inPrisonWorkOther: String? = "Any in-prison work",
  inPrisonEducation: Set<InPrisonTrainingType>? = setOf(InPrisonTrainingType.OTHER),
  inPrisonEducationOther: String? = "Any in-prison training",
  modifiedBy: String = "auser_gen",
  modifiedDateTime: OffsetDateTime = OffsetDateTime.now(),
): PrisonWorkAndEducationResponse =
  PrisonWorkAndEducationResponse(
    id = id,
    inPrisonWork = inPrisonWork,
    inPrisonWorkOther = inPrisonWorkOther,
    inPrisonEducation = inPrisonEducation,
    inPrisonEducationOther = inPrisonEducationOther,
    modifiedBy = modifiedBy,
    modifiedDateTime = modifiedDateTime,
  )
