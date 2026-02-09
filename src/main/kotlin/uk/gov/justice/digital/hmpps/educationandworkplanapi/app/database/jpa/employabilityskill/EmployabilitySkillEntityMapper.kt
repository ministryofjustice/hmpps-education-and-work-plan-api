package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.employabilityskill

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.personallearningplan.EmployabilitySkill
import uk.gov.justice.digital.hmpps.domain.personallearningplan.EmployabilitySkillType
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateEmployabilitySkillsDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.EmployabilitySkillDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.employabilityskill.EmployabilitySkillEntity
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.EmployabilitySkillType as DomainEmployabilitySkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.employabilityskill.EmployabilitySkillType as EntityEmployabilitySkillType

@Component
class EmployabilitySkillEntityMapper {

  fun fromDtoToEntity(createEmployabilitySkillsDto: CreateEmployabilitySkillsDto): List<EmployabilitySkillEntity> = createEmployabilitySkillsDto.employabilitySkills.map { fromDTOToEntity(it) }

  fun fromEntityToDomain(employabilitySkillEntity: EmployabilitySkillEntity): EmployabilitySkill = with(employabilitySkillEntity) {
    EmployabilitySkill(
      reference = reference,
      prisonNumber = prisonNumber,
      employabilitySkillType = skillType.toDomain(),
      ratingCode = ratingCode,
      activityName = activityName,
      evidence = evidence,
      createdAtPrison = createdAtPrison!!,
      updatedAtPrison = updatedAtPrison!!,
      createdBy = createdBy!!,
      updatedBy = updatedBy!!,
      createdAt = createdAt!!,
      updatedAt = updatedAt!!,
      conversationDate = conversationDate,
    )
  }

  fun fromDTOToEntity(dto: EmployabilitySkillDto): EmployabilitySkillEntity = with(dto) {
    EmployabilitySkillEntity(
      prisonNumber = prisonNumber,
      skillType = employabilitySkillType.toEntity(),
      evidence = evidence,
      ratingCode = employabilitySkillRating.name,
      activityName = activityName,
      conversationDate = conversationDate,
      createdAtPrison = prisonId,
      updatedAtPrison = prisonId,
    )
  }
}

private fun EntityEmployabilitySkillType.toDomain(): EmployabilitySkillType = EmployabilitySkillType.valueOf(this.name)

private fun DomainEmployabilitySkillType.toEntity(): EntityEmployabilitySkillType = EntityEmployabilitySkillType.valueOf(this.name)
