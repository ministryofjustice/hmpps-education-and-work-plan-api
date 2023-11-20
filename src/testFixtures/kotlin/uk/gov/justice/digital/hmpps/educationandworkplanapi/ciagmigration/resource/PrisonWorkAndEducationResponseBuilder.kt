package uk.gov.justice.digital.hmpps.educationandworkplanapi.ciagmigration.resource

import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.InPrisonTrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.InPrisonWorkType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.PrisonWorkAndEducationResponse
import java.time.LocalDateTime

fun aValidPrisonWorkAndEducationResponse(
  inPrisonWork: Set<InPrisonWorkType>? = setOf(InPrisonWorkType.OTHER),
  inPrisonWorkOther: String? = "Any in-prison work",
  inPrisonEducation: Set<InPrisonTrainingType>? = setOf(InPrisonTrainingType.OTHER),
  inPrisonEducationOther: String? = "Any in-prison training",
  modifiedBy: String = "auser_gen",
  modifiedDateTime: LocalDateTime = LocalDateTime.now(),
): PrisonWorkAndEducationResponse =
  PrisonWorkAndEducationResponse(
    inPrisonWork = inPrisonWork,
    inPrisonWorkOther = inPrisonWorkOther,
    inPrisonEducation = inPrisonEducation,
    inPrisonEducationOther = inPrisonEducationOther,
    modifiedBy = modifiedBy,
    modifiedDateTime = modifiedDateTime,
  )
