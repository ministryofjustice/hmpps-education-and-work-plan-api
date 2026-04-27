package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.employabilityskill

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.EmployabilitySkill
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.EmployabilitySkillRating
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.EmployabilitySkillSessionType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.EmployabilitySkillType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.dto.CreateEmployabilitySkillsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.dto.EmployabilitySkillDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.employabilityskill.EmployabilitySkillEntity
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.dto.EmployabilitySkillRating as DomainEmployabilitySkillRating
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.dto.EmployabilitySkillSessionType as DomainEmployabilitySkillSessionType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.dto.EmployabilitySkillType as DomainEmployabilitySkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.employabilityskill.EmployabilitySkillRating as EntityEmployabilitySkillRating
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.employabilityskill.EmployabilitySkillSessionType as EntityEmployabilitySkillSessionType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.employabilityskill.EmployabilitySkillType as EntityEmployabilitySkillType

@Component
class EmployabilitySkillEntityMapper {

  fun fromDtoToEntity(createEmployabilitySkillsDto: CreateEmployabilitySkillsDto): List<EmployabilitySkillEntity> = createEmployabilitySkillsDto.employabilitySkills.map { fromDTOToEntity(it) }

  fun fromEntityToDomain(employabilitySkillEntity: EmployabilitySkillEntity): EmployabilitySkill = with(employabilitySkillEntity) {
    EmployabilitySkill(
      reference = reference,
      prisonNumber = prisonNumber,
      employabilitySkillType = skillType.toDomain(),
      sessionType = sessionType?.toDomain(),
      sessionTypeDescription = sessionTypeDescription,
      rating = rating.toDomain(),
      evidence = evidence,
      createdAtPrison = createdAtPrison!!,
      updatedAtPrison = updatedAtPrison!!,
      createdBy = createdBy!!,
      updatedBy = updatedBy!!,
      createdAt = createdAt!!,
      updatedAt = updatedAt!!,
    )
  }

  fun fromDTOToEntity(dto: EmployabilitySkillDto): EmployabilitySkillEntity = with(dto) {
    EmployabilitySkillEntity(
      prisonNumber = prisonNumber,
      skillType = employabilitySkillType.toEntity(),
      evidence = evidence,
      rating = employabilitySkillRating.toEntity(),
      sessionType = sessionType?.toEntity(),
      sessionTypeDescription = sessionTypeDescription,
      createdAtPrison = prisonId,
      updatedAtPrison = prisonId,
    )
  }
}

private fun EntityEmployabilitySkillType.toDomain(): EmployabilitySkillType = when (this) {
  EntityEmployabilitySkillType.TEAMWORK -> EmployabilitySkillType.TEAMWORK
  EntityEmployabilitySkillType.TIMEKEEPING -> EmployabilitySkillType.TIMEKEEPING
  EntityEmployabilitySkillType.COMMUNICATION -> EmployabilitySkillType.COMMUNICATION
  EntityEmployabilitySkillType.PLANNING -> EmployabilitySkillType.PLANNING
  EntityEmployabilitySkillType.ORGANISATION -> EmployabilitySkillType.ORGANISATION
  EntityEmployabilitySkillType.PROBLEM_SOLVING -> EmployabilitySkillType.PROBLEM_SOLVING
  EntityEmployabilitySkillType.INITIATIVE -> EmployabilitySkillType.INITIATIVE
  EntityEmployabilitySkillType.ADAPTABILITY -> EmployabilitySkillType.ADAPTABILITY
  EntityEmployabilitySkillType.RELIABILITY -> EmployabilitySkillType.RELIABILITY
  EntityEmployabilitySkillType.CREATIVITY -> EmployabilitySkillType.CREATIVITY
}

private fun DomainEmployabilitySkillType.toEntity(): EntityEmployabilitySkillType = when (this) {
  DomainEmployabilitySkillType.TEAMWORK -> EntityEmployabilitySkillType.TEAMWORK
  DomainEmployabilitySkillType.TIMEKEEPING -> EntityEmployabilitySkillType.TIMEKEEPING
  DomainEmployabilitySkillType.COMMUNICATION -> EntityEmployabilitySkillType.COMMUNICATION
  DomainEmployabilitySkillType.PLANNING -> EntityEmployabilitySkillType.PLANNING
  DomainEmployabilitySkillType.ORGANISATION -> EntityEmployabilitySkillType.ORGANISATION
  DomainEmployabilitySkillType.PROBLEM_SOLVING -> EntityEmployabilitySkillType.PROBLEM_SOLVING
  DomainEmployabilitySkillType.INITIATIVE -> EntityEmployabilitySkillType.INITIATIVE
  DomainEmployabilitySkillType.ADAPTABILITY -> EntityEmployabilitySkillType.ADAPTABILITY
  DomainEmployabilitySkillType.RELIABILITY -> EntityEmployabilitySkillType.RELIABILITY
  DomainEmployabilitySkillType.CREATIVITY -> EntityEmployabilitySkillType.CREATIVITY
}

private fun EntityEmployabilitySkillSessionType.toDomain(): EmployabilitySkillSessionType = when (this) {
  EntityEmployabilitySkillSessionType.CIAG_INDUCTION -> EmployabilitySkillSessionType.CIAG_INDUCTION
  EntityEmployabilitySkillSessionType.CIAG_REVIEW -> EmployabilitySkillSessionType.CIAG_REVIEW
  EntityEmployabilitySkillSessionType.EDUCATION_REVIEW -> EmployabilitySkillSessionType.EDUCATION_REVIEW
  EntityEmployabilitySkillSessionType.INDUSTRIES_REVIEW -> EmployabilitySkillSessionType.INDUSTRIES_REVIEW
}

private fun DomainEmployabilitySkillSessionType.toEntity(): EntityEmployabilitySkillSessionType = when (this) {
  DomainEmployabilitySkillSessionType.CIAG_INDUCTION -> EntityEmployabilitySkillSessionType.CIAG_INDUCTION
  DomainEmployabilitySkillSessionType.CIAG_REVIEW -> EntityEmployabilitySkillSessionType.CIAG_REVIEW
  DomainEmployabilitySkillSessionType.EDUCATION_REVIEW -> EntityEmployabilitySkillSessionType.EDUCATION_REVIEW
  DomainEmployabilitySkillSessionType.INDUSTRIES_REVIEW -> EntityEmployabilitySkillSessionType.INDUSTRIES_REVIEW
}

private fun EntityEmployabilitySkillRating.toDomain(): EmployabilitySkillRating = when (this) {
  EntityEmployabilitySkillRating.NOT_CONFIDENT -> EmployabilitySkillRating.NOT_CONFIDENT
  EntityEmployabilitySkillRating.LITTLE_CONFIDENCE -> EmployabilitySkillRating.LITTLE_CONFIDENCE
  EntityEmployabilitySkillRating.QUITE_CONFIDENT -> EmployabilitySkillRating.QUITE_CONFIDENT
  EntityEmployabilitySkillRating.VERY_CONFIDENT -> EmployabilitySkillRating.VERY_CONFIDENT
}

private fun DomainEmployabilitySkillRating.toEntity(): EntityEmployabilitySkillRating = when (this) {
  DomainEmployabilitySkillRating.NOT_CONFIDENT -> EntityEmployabilitySkillRating.NOT_CONFIDENT
  DomainEmployabilitySkillRating.LITTLE_CONFIDENCE -> EntityEmployabilitySkillRating.LITTLE_CONFIDENCE
  DomainEmployabilitySkillRating.QUITE_CONFIDENT -> EntityEmployabilitySkillRating.QUITE_CONFIDENT
  DomainEmployabilitySkillRating.VERY_CONFIDENT -> EntityEmployabilitySkillRating.VERY_CONFIDENT
}
