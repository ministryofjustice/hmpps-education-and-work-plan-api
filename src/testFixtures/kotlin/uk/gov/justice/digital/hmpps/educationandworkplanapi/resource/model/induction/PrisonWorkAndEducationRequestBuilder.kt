import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonTrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonWorkType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PrisonWorkAndEducationRequest

fun aValidPrisonWorkAndEducationRequest(
  inPrisonWork: Set<InPrisonWorkType>? = setOf(InPrisonWorkType.OTHER),
  inPrisonWorkOther: String? = "Any in-prison work",
  inPrisonEducation: Set<InPrisonTrainingType>? = setOf(InPrisonTrainingType.OTHER),
  inPrisonEducationOther: String? = "Any in-prison training",
): PrisonWorkAndEducationRequest =
  PrisonWorkAndEducationRequest(
    inPrisonWork = inPrisonWork,
    inPrisonWorkOther = inPrisonWorkOther,
    inPrisonEducation = inPrisonEducation,
    inPrisonEducationOther = inPrisonEducationOther,
  )
