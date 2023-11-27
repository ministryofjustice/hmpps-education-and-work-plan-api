package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkInterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.UpdateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateWorkInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateWorkInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkInterestDetail
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType

@Component
class FutureWorkInterestsResourceMapper {
  fun toCreateFutureWorkInterestsDto(request: CreateWorkInterestsRequest?, prisonId: String): CreateFutureWorkInterestsDto? =
    request?.let {
      CreateFutureWorkInterestsDto(
        interests = toWorkInterests(it.workInterests, it.particularJobInterests, it.workInterestsOther),
        prisonId = prisonId,
      )
    }

  fun toUpdateFutureWorkInterestsDto(request: UpdateWorkInterestsRequest?, prisonId: String): UpdateFutureWorkInterestsDto? =
    request?.let {
      UpdateFutureWorkInterestsDto(
        reference = it.id,
        interests = toWorkInterests(it.workInterests, it.particularJobInterests, it.workInterestsOther),
        prisonId = prisonId,
      )
    }

  private fun toWorkInterests(
    workInterests: Set<WorkType>?,
    particularWorkInterests: Set<WorkInterestDetail>?,
    workInterestsOther: String?,
  ): List<WorkInterest> {
    return workInterests?.map {
      val workType = toWorkInterestType(it)
      val workTypeOther = if (isOtherWorkType(it)) workInterestsOther else null
      val role = particularWorkInterests?.firstOrNull { particularInterest -> particularInterest.workInterest == it }?.role
      WorkInterest(
        workType = workType,
        workTypeOther = workTypeOther,
        role = role,
      )
    } ?: emptyList()
  }

  private fun toWorkInterestType(workType: WorkType): WorkInterestType =
    WorkInterestType.valueOf(workType.name)

  private fun isOtherWorkType(workType: WorkType) =
    workType == WorkType.OTHER
}
