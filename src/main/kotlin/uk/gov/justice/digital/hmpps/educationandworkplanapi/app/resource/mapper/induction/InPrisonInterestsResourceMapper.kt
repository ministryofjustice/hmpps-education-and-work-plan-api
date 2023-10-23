package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InPrisonTrainingInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InPrisonWorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PrisonWorkAndEducationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InPrisonTrainingType as InPrisonTrainingTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InPrisonWorkType as InPrisonWorkTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonTrainingType as InPrisonTrainingTypeApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonWorkType as InPrisonWorkTypeApi

@Component
class InPrisonInterestsResourceMapper {

  fun toCreateInPrisonInterestsDto(request: PrisonWorkAndEducationRequest?, prisonId: String): CreateInPrisonInterestsDto? {
    return request?.let {
      CreateInPrisonInterestsDto(
        inPrisonWorkInterests = toInPrisonWorkInterests(it.inPrisonWork, it.inPrisonWorkOther),
        inPrisonTrainingInterests = toInPrisonTrainingInterests(it.inPrisonEducation, it.inPrisonEducationOther),
        prisonId = prisonId,
      )
    }
  }

  private fun toInPrisonWorkInterests(
    inPrisonWorkTypes: Set<InPrisonWorkTypeApi>?,
    inPrisonWorkOther: String?,
  ): List<InPrisonWorkInterest> {
    return inPrisonWorkTypes?.map {
      val workType = InPrisonWorkTypeDomain.valueOf(it.name)
      val workTypeOther = if (it == InPrisonWorkTypeApi.OTHER) inPrisonWorkOther else null
      InPrisonWorkInterest(workType = workType, workTypeOther = workTypeOther)
    } ?: emptyList()
  }

  private fun toInPrisonTrainingInterests(
    inPrisonEducationTypes: Set<InPrisonTrainingTypeApi>?,
    inPrisonEducationOther: String?,
  ): List<InPrisonTrainingInterest> {
    return inPrisonEducationTypes?.map {
      val trainingType = InPrisonTrainingTypeDomain.valueOf(it.name)
      val trainingTypeOther = if (it == InPrisonTrainingTypeApi.OTHER) inPrisonEducationOther else null
      InPrisonTrainingInterest(trainingType = trainingType, trainingTypeOther = trainingTypeOther)
    } ?: emptyList()
  }
}
