package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkOnRelease
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateWorkOnReleaseRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateWorkOnReleaseRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkOnReleaseResponse
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.AffectAbilityToWork as AffectAbilityToWorkDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HopingToWork as HopingToWorkDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AffectAbilityToWork as AffectAbilityToWorkApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork as HopingToWorkApi

@Component
class WorkOnReleaseResourceMapper(
  private val instantMapper: InstantMapper,
  private val userService: ManageUserService,
) {
  fun toCreateWorkOnReleaseDto(request: CreateWorkOnReleaseRequest, prisonId: String): CreateWorkOnReleaseDto = with(request) {
    CreateWorkOnReleaseDto(
      prisonId = prisonId,
      hopingToWork = toHopingToWork(hopingToWork),
      affectAbilityToWork = affectAbilityToWork?.map { toAffectAbilityToWork(it) }.orEmpty(),
      affectAbilityToWorkOther = affectAbilityToWorkOther,
    )
  }

  fun toWorkOnReleaseResponse(workOnRelease: WorkOnRelease): WorkOnReleaseResponse = with(workOnRelease) {
    WorkOnReleaseResponse(
      reference = reference,
      hopingToWork = toHopingToWork(hopingToWork),
      affectAbilityToWork = affectAbilityToWork.map { toAffectAbilityToWork(it) },
      affectAbilityToWorkOther = affectAbilityToWorkOther,
      createdBy = createdBy!!,
      createdByDisplayName = userService.getUserDetails(createdBy!!).name,
      createdAt = instantMapper.toOffsetDateTime(createdAt)!!,
      createdAtPrison = createdAtPrison,
      updatedBy = lastUpdatedBy!!,
      updatedByDisplayName = userService.getUserDetails(lastUpdatedBy!!).name,
      updatedAt = instantMapper.toOffsetDateTime(lastUpdatedAt)!!,
      updatedAtPrison = lastUpdatedAtPrison,
    )
  }

  fun toUpdateWorkOnReleaseDto(request: UpdateWorkOnReleaseRequest, prisonId: String): UpdateWorkOnReleaseDto = with(request) {
    UpdateWorkOnReleaseDto(
      reference = reference,
      prisonId = prisonId,
      hopingToWork = toHopingToWork(hopingToWork),
      affectAbilityToWork = affectAbilityToWork?.map { toAffectAbilityToWork(it) }.orEmpty(),
      affectAbilityToWorkOther = affectAbilityToWorkOther,
    )
  }

  private fun toAffectAbilityToWork(affectAbilityToWork: AffectAbilityToWorkApi): AffectAbilityToWorkDomain = when (affectAbilityToWork) {
    AffectAbilityToWorkApi.LIMITED_BY_OFFENCE -> AffectAbilityToWorkDomain.LIMITED_BY_OFFENCE
    AffectAbilityToWorkApi.CARING_RESPONSIBILITIES -> AffectAbilityToWorkDomain.CARING_RESPONSIBILITIES
    AffectAbilityToWorkApi.NEEDS_WORK_ADJUSTMENTS_DUE_TO_HEALTH -> AffectAbilityToWorkDomain.NEEDS_WORK_ADJUSTMENTS_DUE_TO_HEALTH
    AffectAbilityToWorkApi.UNABLE_TO_WORK_DUE_TO_HEALTH -> AffectAbilityToWorkDomain.UNABLE_TO_WORK_DUE_TO_HEALTH
    AffectAbilityToWorkApi.LACKS_CONFIDENCE_OR_MOTIVATION -> AffectAbilityToWorkDomain.LACKS_CONFIDENCE_OR_MOTIVATION
    AffectAbilityToWorkApi.REFUSED_SUPPORT_WITH_NO_REASON -> AffectAbilityToWorkDomain.REFUSED_SUPPORT_WITH_NO_REASON
    AffectAbilityToWorkApi.RETIRED -> AffectAbilityToWorkDomain.RETIRED
    AffectAbilityToWorkApi.NO_RIGHT_TO_WORK -> AffectAbilityToWorkDomain.NO_RIGHT_TO_WORK
    AffectAbilityToWorkApi.NOT_SURE -> AffectAbilityToWorkDomain.NOT_SURE
    AffectAbilityToWorkApi.OTHER -> AffectAbilityToWorkDomain.OTHER
    AffectAbilityToWorkApi.NONE -> AffectAbilityToWorkDomain.NONE
  }

  private fun toAffectAbilityToWork(affectAbilityToWork: AffectAbilityToWorkDomain): AffectAbilityToWorkApi = when (affectAbilityToWork) {
    AffectAbilityToWorkDomain.LIMITED_BY_OFFENCE -> AffectAbilityToWorkApi.LIMITED_BY_OFFENCE
    AffectAbilityToWorkDomain.CARING_RESPONSIBILITIES -> AffectAbilityToWorkApi.CARING_RESPONSIBILITIES
    AffectAbilityToWorkDomain.NEEDS_WORK_ADJUSTMENTS_DUE_TO_HEALTH -> AffectAbilityToWorkApi.NEEDS_WORK_ADJUSTMENTS_DUE_TO_HEALTH
    AffectAbilityToWorkDomain.UNABLE_TO_WORK_DUE_TO_HEALTH -> AffectAbilityToWorkApi.UNABLE_TO_WORK_DUE_TO_HEALTH
    AffectAbilityToWorkDomain.LACKS_CONFIDENCE_OR_MOTIVATION -> AffectAbilityToWorkApi.LACKS_CONFIDENCE_OR_MOTIVATION
    AffectAbilityToWorkDomain.REFUSED_SUPPORT_WITH_NO_REASON -> AffectAbilityToWorkApi.REFUSED_SUPPORT_WITH_NO_REASON
    AffectAbilityToWorkDomain.RETIRED -> AffectAbilityToWorkApi.RETIRED
    AffectAbilityToWorkDomain.NO_RIGHT_TO_WORK -> AffectAbilityToWorkApi.NO_RIGHT_TO_WORK
    AffectAbilityToWorkDomain.NOT_SURE -> AffectAbilityToWorkApi.NOT_SURE
    AffectAbilityToWorkDomain.OTHER -> AffectAbilityToWorkApi.OTHER
    AffectAbilityToWorkDomain.NONE -> AffectAbilityToWorkApi.NONE
  }

  private fun toHopingToWork(hopingToWork: HopingToWorkApi): HopingToWorkDomain = when (hopingToWork) {
    HopingToWorkApi.YES -> HopingToWorkDomain.YES
    HopingToWorkApi.NO -> HopingToWorkDomain.NO
    HopingToWorkApi.NOT_SURE -> HopingToWorkDomain.NOT_SURE
  }

  private fun toHopingToWork(hopingToWork: HopingToWorkDomain): HopingToWorkApi = when (hopingToWork) {
    HopingToWorkDomain.YES -> HopingToWorkApi.YES
    HopingToWorkDomain.NO -> HopingToWorkApi.NO
    HopingToWorkDomain.NOT_SURE -> HopingToWorkApi.NOT_SURE
  }
}
