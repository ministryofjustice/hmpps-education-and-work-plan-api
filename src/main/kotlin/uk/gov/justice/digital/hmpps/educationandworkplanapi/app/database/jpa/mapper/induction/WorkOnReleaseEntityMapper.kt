package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkOnRelease
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkOnReleaseEntity
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.AffectAbilityToWork as AffectAbilityToWorkDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HopingToWork as HopingToWorkDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork as AffectAbilityToWorkEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HopingToWork as HopingToWorkEntity

@Component
class WorkOnReleaseEntityMapper {

  fun fromCreateDtoToEntity(dto: CreateWorkOnReleaseDto): WorkOnReleaseEntity =
    with(dto) {
      WorkOnReleaseEntity(
        createdAtPrison = prisonId,
        updatedAtPrison = prisonId,
        hopingToWork = toHopingToWork(hopingToWork),
        affectAbilityToWorkOther = affectAbilityToWorkOther,
        reference = UUID.randomUUID(),
      ).apply {
        affectAbilityToWork.addAll(
          dto.affectAbilityToWork.map { toAffectAbilityToWork(it) },
        )
      }
    }

  fun fromEntityToDomain(entity: WorkOnReleaseEntity): WorkOnRelease =
    with(entity) {
      WorkOnRelease(
        reference = reference,
        hopingToWork = toHopingToWork(hopingToWork),
        affectAbilityToWork = affectAbilityToWork.map { toAffectAbilityToWork(it) },
        affectAbilityToWorkOther = affectAbilityToWorkOther,
        createdBy = createdBy,
        createdAt = createdAt,
        createdAtPrison = createdAtPrison,
        lastUpdatedBy = updatedBy,
        lastUpdatedAt = updatedAt,
        lastUpdatedAtPrison = updatedAtPrison,
      )
    }

  fun updateExistingEntityFromDto(entity: WorkOnReleaseEntity, dto: UpdateWorkOnReleaseDto) =
    with(entity) {
      updatedAtPrison = dto.prisonId
      hopingToWork = toHopingToWork(dto.hopingToWork)
      affectAbilityToWorkOther = dto.affectAbilityToWorkOther

      affectAbilityToWork.clear()
      affectAbilityToWork.addAll(
        dto.affectAbilityToWork.map { toAffectAbilityToWork(it) },
      )
    }

  fun toAffectAbilityToWork(affectAbilityToWork: AffectAbilityToWorkEntity): AffectAbilityToWorkDomain =
    when (affectAbilityToWork) {
      AffectAbilityToWorkEntity.LIMITED_BY_OFFENCE -> AffectAbilityToWorkDomain.LIMITED_BY_OFFENCE
      AffectAbilityToWorkEntity.CARING_RESPONSIBILITIES -> AffectAbilityToWorkDomain.CARING_RESPONSIBILITIES
      AffectAbilityToWorkEntity.NEEDS_WORK_ADJUSTMENTS_DUE_TO_HEALTH -> AffectAbilityToWorkDomain.NEEDS_WORK_ADJUSTMENTS_DUE_TO_HEALTH
      AffectAbilityToWorkEntity.UNABLE_TO_WORK_DUE_TO_HEALTH -> AffectAbilityToWorkDomain.UNABLE_TO_WORK_DUE_TO_HEALTH
      AffectAbilityToWorkEntity.LACKS_CONFIDENCE_OR_MOTIVATION -> AffectAbilityToWorkDomain.LACKS_CONFIDENCE_OR_MOTIVATION
      AffectAbilityToWorkEntity.REFUSED_SUPPORT_WITH_NO_REASON -> AffectAbilityToWorkDomain.REFUSED_SUPPORT_WITH_NO_REASON
      AffectAbilityToWorkEntity.RETIRED -> AffectAbilityToWorkDomain.RETIRED
      AffectAbilityToWorkEntity.NO_RIGHT_TO_WORK -> AffectAbilityToWorkDomain.NO_RIGHT_TO_WORK
      AffectAbilityToWorkEntity.NOT_SURE -> AffectAbilityToWorkDomain.NOT_SURE
      AffectAbilityToWorkEntity.OTHER -> AffectAbilityToWorkDomain.OTHER
      AffectAbilityToWorkEntity.NONE -> AffectAbilityToWorkDomain.NONE
    }

  fun toAffectAbilityToWork(affectAbilityToWork: AffectAbilityToWorkDomain): AffectAbilityToWorkEntity =
    when (affectAbilityToWork) {
      AffectAbilityToWorkDomain.LIMITED_BY_OFFENCE -> AffectAbilityToWorkEntity.LIMITED_BY_OFFENCE
      AffectAbilityToWorkDomain.CARING_RESPONSIBILITIES -> AffectAbilityToWorkEntity.CARING_RESPONSIBILITIES
      AffectAbilityToWorkDomain.NEEDS_WORK_ADJUSTMENTS_DUE_TO_HEALTH -> AffectAbilityToWorkEntity.NEEDS_WORK_ADJUSTMENTS_DUE_TO_HEALTH
      AffectAbilityToWorkDomain.UNABLE_TO_WORK_DUE_TO_HEALTH -> AffectAbilityToWorkEntity.UNABLE_TO_WORK_DUE_TO_HEALTH
      AffectAbilityToWorkDomain.LACKS_CONFIDENCE_OR_MOTIVATION -> AffectAbilityToWorkEntity.LACKS_CONFIDENCE_OR_MOTIVATION
      AffectAbilityToWorkDomain.REFUSED_SUPPORT_WITH_NO_REASON -> AffectAbilityToWorkEntity.REFUSED_SUPPORT_WITH_NO_REASON
      AffectAbilityToWorkDomain.RETIRED -> AffectAbilityToWorkEntity.RETIRED
      AffectAbilityToWorkDomain.NO_RIGHT_TO_WORK -> AffectAbilityToWorkEntity.NO_RIGHT_TO_WORK
      AffectAbilityToWorkDomain.NOT_SURE -> AffectAbilityToWorkEntity.NOT_SURE
      AffectAbilityToWorkDomain.OTHER -> AffectAbilityToWorkEntity.OTHER
      AffectAbilityToWorkDomain.NONE -> AffectAbilityToWorkEntity.NONE
    }

  fun toHopingToWork(hopingToWork: HopingToWorkDomain): HopingToWorkEntity =
    when (hopingToWork) {
      HopingToWorkDomain.YES -> HopingToWorkEntity.YES
      HopingToWorkDomain.NO -> HopingToWorkEntity.NO
      HopingToWorkDomain.NOT_SURE -> HopingToWorkEntity.NOT_SURE
    }

  fun toHopingToWork(hopingToWork: HopingToWorkEntity): HopingToWorkDomain =
    when (hopingToWork) {
      HopingToWorkEntity.YES -> HopingToWorkDomain.YES
      HopingToWorkEntity.NO -> HopingToWorkDomain.NO
      HopingToWorkEntity.NOT_SURE -> HopingToWorkDomain.NOT_SURE
    }
}
