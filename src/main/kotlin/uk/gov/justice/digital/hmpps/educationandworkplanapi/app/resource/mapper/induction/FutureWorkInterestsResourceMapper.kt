package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkInterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkInterestDetail
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType

@Component
class FutureWorkInterestsResourceMapper {
  fun toCreateFutureWorkInterestsDto(request: WorkInterests?, prisonId: String): CreateFutureWorkInterestsDto? {
    return request?.let {
      CreateFutureWorkInterestsDto(
        interests = toWorkInterests(it.particularJobInterests, it.workInterestsOther),
        prisonId = prisonId,
      )
    }
  }

  private fun toWorkInterests(
    workInterests: Set<WorkInterestDetail>?,
    workInterestsOther: String?,
  ): List<WorkInterest> {
    return workInterests?.map {
      val workType = toWorkInterestType(it.workInterest)
      val workTypeOther = if (isOtherWorkType(it)) workInterestsOther else null
      WorkInterest(
        workType = workType,
        workTypeOther = workTypeOther,
        role = it.role,
      )
    } ?: emptyList()
  }

  private fun toWorkInterestType(workType: WorkType): WorkInterestType =
    WorkInterestType.valueOf(workType.name)

  private fun isOtherWorkType(workInterestDetail: WorkInterestDetail) =
    workInterestDetail.workInterest == WorkType.OTHER
}
