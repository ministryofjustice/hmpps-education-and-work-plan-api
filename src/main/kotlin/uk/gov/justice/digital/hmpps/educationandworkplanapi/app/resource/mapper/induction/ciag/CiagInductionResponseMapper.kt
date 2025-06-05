package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.ciag

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSummary
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CiagInductionSummaryResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork

@Component
class CiagInductionResponseMapper(private val instantMapper: InstantMapper) {
  fun fromDomainToModel(inductionSummaries: List<InductionSummary>): List<CiagInductionSummaryResponse> = inductionSummaries.map { inductionSummary ->
    with(inductionSummary) {
      CiagInductionSummaryResponse(
        offenderId = prisonNumber,
        desireToWork = false,
        hopingToGetWork = HopingToWork.NO,
        createdBy = createdBy,
        createdDateTime = instantMapper.toOffsetDateTime(createdAt)!!,
        modifiedBy = inductionSummary.lastUpdatedBy,
        modifiedDateTime = instantMapper.toOffsetDateTime(inductionSummary.lastUpdatedAt)!!,
      )
    }
  }
}
