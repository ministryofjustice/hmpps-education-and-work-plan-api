package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkInterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkInterestDetail
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType

@Component
class FutureWorkInterestsMapper {
  fun toFutureWorkInterests(request: WorkInterests?, prisonId: String): CreateFutureWorkInterestsDto? {
    return request?.let {
      CreateFutureWorkInterestsDto(
        interests = toWorkInterests(request.particularJobInterests, request.workInterestsOther),
        prisonId = prisonId,
      )
    }
  }

  private fun toWorkInterests(
    workInterests: Set<WorkInterestDetail>?,
    workInterestsOther: String?,
  ): List<WorkInterest> {
    return workInterests?.map {
      val workType = WorkInterestType.valueOf(it.workInterest.name)
      val workTypeOther = if (it.workInterest == WorkType.OTHER) workInterestsOther else null
      WorkInterest(
        workType = workType,
        workTypeOther = workTypeOther,
        role = it.role,
      )
    } ?: emptyList()
  }
}
