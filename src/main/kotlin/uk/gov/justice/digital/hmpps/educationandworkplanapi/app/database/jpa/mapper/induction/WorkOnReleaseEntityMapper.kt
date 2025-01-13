package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.AffectAbilityToWork
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HopingToWork
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkOnRelease
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkOnReleaseEntity
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork as AffectAbilityToWorkEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HopingToWork as HopingToWorkEntity

@Component
class WorkOnReleaseEntityMapper {

  fun fromCreateDtoToEntity(dto: CreateWorkOnReleaseDto): WorkOnReleaseEntity {
    val workOnReleaseEntity = WorkOnReleaseEntity()
    workOnReleaseEntity.createdAtPrison = dto.prisonId
    workOnReleaseEntity.updatedAtPrison = dto.prisonId
    workOnReleaseEntity.hopingToWork = toHopingToWork(dto.hopingToWork)
    workOnReleaseEntity.affectAbilityToWork = toEntity(dto.affectAbilityToWork)
    workOnReleaseEntity.affectAbilityToWorkOther = dto.affectAbilityToWorkOther

    workOnReleaseEntity.reference = UUID.randomUUID()

    return workOnReleaseEntity
  }

  fun fromEntityToDomain(entity: WorkOnReleaseEntity): WorkOnRelease {
    with(entity) {
      return WorkOnRelease(
        reference!!,
        toHopingToWork(hopingToWork!!),
        toModel(affectAbilityToWork),
        affectAbilityToWorkOther,
        createdBy,
        createdAt,
        createdAtPrison!!,
        updatedBy,
        updatedAt,
        updatedAtPrison!!,
      )
    }
  }

  fun updateExistingEntityFromDto(entity: WorkOnReleaseEntity, dto: UpdateWorkOnReleaseDto?) {
    if (dto == null) {
      return
    }
    entity.updatedAtPrison = dto.prisonId
    entity.hopingToWork = toHopingToWork(dto.hopingToWork)

    val updatedAffectAbilityToWorkList = toEntity(dto.affectAbilityToWork)
    if (entity.affectAbilityToWork != null) {
      entity.affectAbilityToWork!!.clear()
      entity.affectAbilityToWork!!.addAll(updatedAffectAbilityToWorkList)
    } else {
      entity.affectAbilityToWork = updatedAffectAbilityToWorkList
    }
    entity.affectAbilityToWorkOther = dto.affectAbilityToWorkOther
  }

  fun toEntity(list: List<AffectAbilityToWork>): MutableList<AffectAbilityToWorkEntity> {
    return list.mapTo(ArrayList(list.size)) { affectAbilityToWorkToAffectAbilityToWork(it) }
  }

  fun toModel(list: List<AffectAbilityToWorkEntity>?): List<AffectAbilityToWork> {
    return list?.map { toAffectAbilityToWork(it) } ?: emptyList()
  }

  fun toAffectAbilityToWork(affectAbilityToWork: uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork): AffectAbilityToWork {
    return when (affectAbilityToWork) {
      AffectAbilityToWorkEntity.LIMITED_BY_OFFENCE -> AffectAbilityToWork.LIMITED_BY_OFFENCE
      AffectAbilityToWorkEntity.CARING_RESPONSIBILITIES -> AffectAbilityToWork.CARING_RESPONSIBILITIES
      AffectAbilityToWorkEntity.NEEDS_WORK_ADJUSTMENTS_DUE_TO_HEALTH -> AffectAbilityToWork.NEEDS_WORK_ADJUSTMENTS_DUE_TO_HEALTH
      AffectAbilityToWorkEntity.UNABLE_TO_WORK_DUE_TO_HEALTH -> AffectAbilityToWork.UNABLE_TO_WORK_DUE_TO_HEALTH
      AffectAbilityToWorkEntity.LACKS_CONFIDENCE_OR_MOTIVATION -> AffectAbilityToWork.LACKS_CONFIDENCE_OR_MOTIVATION
      AffectAbilityToWorkEntity.REFUSED_SUPPORT_WITH_NO_REASON -> AffectAbilityToWork.REFUSED_SUPPORT_WITH_NO_REASON
      AffectAbilityToWorkEntity.RETIRED -> AffectAbilityToWork.RETIRED
      AffectAbilityToWorkEntity.NO_RIGHT_TO_WORK -> AffectAbilityToWork.NO_RIGHT_TO_WORK
      AffectAbilityToWorkEntity.NOT_SURE -> AffectAbilityToWork.NOT_SURE
      AffectAbilityToWorkEntity.OTHER -> AffectAbilityToWork.OTHER
      AffectAbilityToWorkEntity.NONE -> AffectAbilityToWork.NONE
    }
  }

  fun toHopingToWork(hopingToWork: HopingToWork): HopingToWorkEntity {
    return when (hopingToWork) {
      HopingToWork.YES -> HopingToWorkEntity.YES
      HopingToWork.NO -> HopingToWorkEntity.NO
      HopingToWork.NOT_SURE -> HopingToWorkEntity.NOT_SURE
    }
  }

  fun affectAbilityToWorkToAffectAbilityToWork(affectAbilityToWork: AffectAbilityToWork): uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork {
    return when (affectAbilityToWork) {
      AffectAbilityToWork.LIMITED_BY_OFFENCE -> AffectAbilityToWorkEntity.LIMITED_BY_OFFENCE
      AffectAbilityToWork.CARING_RESPONSIBILITIES -> AffectAbilityToWorkEntity.CARING_RESPONSIBILITIES
      AffectAbilityToWork.NEEDS_WORK_ADJUSTMENTS_DUE_TO_HEALTH -> AffectAbilityToWorkEntity.NEEDS_WORK_ADJUSTMENTS_DUE_TO_HEALTH
      AffectAbilityToWork.UNABLE_TO_WORK_DUE_TO_HEALTH -> AffectAbilityToWorkEntity.UNABLE_TO_WORK_DUE_TO_HEALTH
      AffectAbilityToWork.LACKS_CONFIDENCE_OR_MOTIVATION -> AffectAbilityToWorkEntity.LACKS_CONFIDENCE_OR_MOTIVATION
      AffectAbilityToWork.REFUSED_SUPPORT_WITH_NO_REASON -> AffectAbilityToWorkEntity.REFUSED_SUPPORT_WITH_NO_REASON
      AffectAbilityToWork.RETIRED -> AffectAbilityToWorkEntity.RETIRED
      AffectAbilityToWork.NO_RIGHT_TO_WORK -> AffectAbilityToWorkEntity.NO_RIGHT_TO_WORK
      AffectAbilityToWork.NOT_SURE -> AffectAbilityToWorkEntity.NOT_SURE
      AffectAbilityToWork.OTHER -> AffectAbilityToWorkEntity.OTHER
      AffectAbilityToWork.NONE -> AffectAbilityToWorkEntity.NONE
    }
  }

  fun toHopingToWork(hopingToWork: HopingToWorkEntity): HopingToWork {
    return when (hopingToWork) {
      HopingToWorkEntity.YES -> HopingToWork.YES
      HopingToWorkEntity.NO -> HopingToWork.NO
      HopingToWorkEntity.NOT_SURE -> HopingToWork.NOT_SURE
    }
  }
}
