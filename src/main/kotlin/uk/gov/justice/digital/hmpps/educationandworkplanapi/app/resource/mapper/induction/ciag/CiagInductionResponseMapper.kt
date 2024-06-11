package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.ciag

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSummary
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CiagInductionSummaryResponse
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HopingToWork as HopingToWorkDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork as HopingToWorkApi

@Component
class CiagInductionResponseMapper(private val instantMapper: InstantMapper) {
  fun fromDomainToModel(inductionSummaries: List<InductionSummary>): List<CiagInductionSummaryResponse> {
    return inductionSummaries.map { inductionSummary ->
      with(inductionSummary) {
        CiagInductionSummaryResponse(
          offenderId = prisonNumber,
          desireToWork = when (workOnRelease.hopingToWork) {
            HopingToWorkDomain.YES -> true
            else -> false
          },
          hopingToGetWork = HopingToWorkApi.valueOf(workOnRelease.hopingToWork.name),
          createdBy = createdBy,
          createdDateTime = instantMapper.toOffsetDateTime(createdAt)!!,
          // The main Induction domain object is immutable (the data that can be changed belongs in associated "child"
          // objects). However, the CIAG API stores details regarding a prisoner's work aspirations at the root level,
          // which means that any changes to these values needs to be reflected in the "modified" fields of the root
          // Induction in the response.
          modifiedBy = workOnRelease.lastUpdatedBy!!,
          modifiedDateTime = instantMapper.toOffsetDateTime(workOnRelease.lastUpdatedAt)!!,
        )
      }
    }
  }
}
