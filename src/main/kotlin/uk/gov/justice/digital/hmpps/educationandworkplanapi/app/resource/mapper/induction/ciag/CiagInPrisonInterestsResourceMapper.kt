package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.ciag

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InPrisonInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InPrisonTrainingInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InPrisonWorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.UpdateInPrisonInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePrisonWorkAndEducationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonTrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonWorkType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PrisonWorkAndEducationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePrisonWorkAndEducationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InPrisonTrainingType as InPrisonTrainingTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InPrisonWorkType as InPrisonWorkTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonTrainingType as InPrisonTrainingTypeApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InPrisonWorkType as InPrisonWorkTypeApi

@Component
class CiagInPrisonInterestsResourceMapper(
  val instantMapper: InstantMapper,
) {

  fun toCreateInPrisonInterestsDto(
    request: CreatePrisonWorkAndEducationRequest?,
    prisonId: String,
  ): CreateInPrisonInterestsDto? =
    request?.let {
      CreateInPrisonInterestsDto(
        inPrisonWorkInterests = toInPrisonWorkInterests(it.inPrisonWork, it.inPrisonWorkOther),
        inPrisonTrainingInterests = toInPrisonTrainingInterests(it.inPrisonEducation, it.inPrisonEducationOther),
        prisonId = prisonId,
      )
    }

  fun toPrisonWorkAndEducationResponse(inPrisonInterests: InPrisonInterests?): PrisonWorkAndEducationResponse? =
    inPrisonInterests?.let {
      PrisonWorkAndEducationResponse(
        id = inPrisonInterests.reference,
        inPrisonWork = toInPrisonWorkTypes(inPrisonInterests.inPrisonWorkInterests),
        inPrisonWorkOther = toInPrisonWorkOther(inPrisonInterests),
        inPrisonEducation = toInPrisonTrainingTypes(inPrisonInterests.inPrisonTrainingInterests),
        inPrisonEducationOther = toInPrisonTrainingOther(inPrisonInterests),
        modifiedBy = inPrisonInterests.lastUpdatedBy!!,
        modifiedDateTime = instantMapper.toOffsetDateTime(inPrisonInterests.lastUpdatedAt)!!,
      )
    }

  private fun toInPrisonWorkInterests(
    inPrisonWorkTypes: Set<InPrisonWorkTypeApi>?,
    inPrisonWorkOther: String?,
  ): List<InPrisonWorkInterest> =
    inPrisonWorkTypes?.map {
      val workType = InPrisonWorkTypeDomain.valueOf(it.name)
      val workTypeOther = if (it == InPrisonWorkTypeApi.OTHER) inPrisonWorkOther else null
      InPrisonWorkInterest(workType = workType, workTypeOther = workTypeOther)
    } ?: emptyList()

  private fun toInPrisonTrainingInterests(
    inPrisonEducationTypes: Set<InPrisonTrainingTypeApi>?,
    inPrisonEducationOther: String?,
  ): List<InPrisonTrainingInterest> =
    inPrisonEducationTypes?.map {
      val trainingType = InPrisonTrainingTypeDomain.valueOf(it.name)
      val trainingTypeOther = if (it == InPrisonTrainingTypeApi.OTHER) inPrisonEducationOther else null
      InPrisonTrainingInterest(trainingType = trainingType, trainingTypeOther = trainingTypeOther)
    } ?: emptyList()

  private fun toInPrisonWorkTypes(workInterests: List<InPrisonWorkInterest>): Set<InPrisonWorkType>? =
    workInterests.map { InPrisonWorkType.valueOf(it.workType.name) }.toSet()

  private fun toInPrisonTrainingTypes(trainingInterests: List<InPrisonTrainingInterest>): Set<InPrisonTrainingType>? =
    trainingInterests.map { InPrisonTrainingType.valueOf(it.trainingType.name) }.toSet()

  private fun toInPrisonWorkOther(inPrisonInterests: InPrisonInterests) =
    inPrisonInterests.inPrisonWorkInterests.firstOrNull { it.workType == InPrisonWorkTypeDomain.OTHER }?.workTypeOther

  private fun toInPrisonTrainingOther(inPrisonInterests: InPrisonInterests) =
    inPrisonInterests.inPrisonTrainingInterests.firstOrNull { it.trainingType == InPrisonTrainingTypeDomain.OTHER }?.trainingTypeOther

  fun toUpdateInPrisonInterestsDto(
    request: UpdatePrisonWorkAndEducationRequest?,
    prisonId: String,
  ): UpdateInPrisonInterestsDto? =
    request?.let {
      UpdateInPrisonInterestsDto(
        reference = it.id,
        inPrisonWorkInterests = toInPrisonWorkInterests(it.inPrisonWork, it.inPrisonWorkOther),
        inPrisonTrainingInterests = toInPrisonTrainingInterests(it.inPrisonEducation, it.inPrisonEducationOther),
        prisonId = prisonId,
      )
    }
}
