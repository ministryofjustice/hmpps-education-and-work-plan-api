package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.AffectAbilityToWork
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HopingToWork
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkOnRelease
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkOnReleaseEntity
import java.util.UUID

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
        createdByDisplayName,
        createdAt,
        createdAtPrison!!,
        updatedBy,
        updatedByDisplayName,
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

  fun toEntity(list: List<AffectAbilityToWork>): MutableList<uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork> {
    return list.mapTo(ArrayList(list.size)) { affectAbilityToWorkToAffectAbilityToWork(it) }
  }

  fun toModel(list: List<uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork>?): List<AffectAbilityToWork> {
    return list?.map { toAffectAbilityToWork(it) } ?: emptyList()
  }

  fun toAffectAbilityToWork(affectAbilityToWork: uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork): AffectAbilityToWork {
    return when (affectAbilityToWork) {
      uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork.LIMITED_BY_OFFENCE -> AffectAbilityToWork.LIMITED_BY_OFFENCE
      uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork.CARING_RESPONSIBILITIES -> AffectAbilityToWork.CARING_RESPONSIBILITIES
      uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork.NEEDS_WORK_ADJUSTMENTS_DUE_TO_HEALTH -> AffectAbilityToWork.NEEDS_WORK_ADJUSTMENTS_DUE_TO_HEALTH
      uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork.UNABLE_TO_WORK_DUE_TO_HEALTH -> AffectAbilityToWork.UNABLE_TO_WORK_DUE_TO_HEALTH
      uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork.LACKS_CONFIDENCE_OR_MOTIVATION -> AffectAbilityToWork.LACKS_CONFIDENCE_OR_MOTIVATION
      uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork.REFUSED_SUPPORT_WITH_NO_REASON -> AffectAbilityToWork.REFUSED_SUPPORT_WITH_NO_REASON
      uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork.RETIRED -> AffectAbilityToWork.RETIRED
      uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork.NO_RIGHT_TO_WORK -> AffectAbilityToWork.NO_RIGHT_TO_WORK
      uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork.NOT_SURE -> AffectAbilityToWork.NOT_SURE
      uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork.OTHER -> AffectAbilityToWork.OTHER
      uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork.NONE -> AffectAbilityToWork.NONE
    }
  }

  fun toHopingToWork(hopingToWork: HopingToWork): uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HopingToWork {
    return when (hopingToWork) {
      HopingToWork.YES -> uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HopingToWork.YES
      HopingToWork.NO -> uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HopingToWork.NO
      HopingToWork.NOT_SURE -> uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HopingToWork.NOT_SURE
      else -> throw IllegalArgumentException("Unexpected enum constant: $hopingToWork")
    }
  }

  fun affectAbilityToWorkToAffectAbilityToWork(affectAbilityToWork: AffectAbilityToWork): uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork {
    return when (affectAbilityToWork) {
      AffectAbilityToWork.LIMITED_BY_OFFENCE -> uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork.LIMITED_BY_OFFENCE
      AffectAbilityToWork.CARING_RESPONSIBILITIES -> uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork.CARING_RESPONSIBILITIES
      AffectAbilityToWork.NEEDS_WORK_ADJUSTMENTS_DUE_TO_HEALTH -> uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork.NEEDS_WORK_ADJUSTMENTS_DUE_TO_HEALTH
      AffectAbilityToWork.UNABLE_TO_WORK_DUE_TO_HEALTH -> uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork.UNABLE_TO_WORK_DUE_TO_HEALTH
      AffectAbilityToWork.LACKS_CONFIDENCE_OR_MOTIVATION -> uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork.LACKS_CONFIDENCE_OR_MOTIVATION
      AffectAbilityToWork.REFUSED_SUPPORT_WITH_NO_REASON -> uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork.REFUSED_SUPPORT_WITH_NO_REASON
      AffectAbilityToWork.RETIRED -> uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork.RETIRED
      AffectAbilityToWork.NO_RIGHT_TO_WORK -> uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork.NO_RIGHT_TO_WORK
      AffectAbilityToWork.NOT_SURE -> uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork.NOT_SURE
      AffectAbilityToWork.OTHER -> uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork.OTHER
      AffectAbilityToWork.NONE -> uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork.NONE
    }
  }

  fun toHopingToWork(hopingToWork: uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HopingToWork): HopingToWork {
    return when (hopingToWork) {
      uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HopingToWork.YES -> HopingToWork.YES
      uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HopingToWork.NO -> HopingToWork.NO
      uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HopingToWork.NOT_SURE -> HopingToWork.NOT_SURE
    }
  }
}
