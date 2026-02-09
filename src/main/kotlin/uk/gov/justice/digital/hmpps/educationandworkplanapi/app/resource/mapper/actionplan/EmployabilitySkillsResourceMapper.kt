package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateEmployabilitySkillsDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.EmployabilitySkillDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateEmployabilitySkillRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateEmployabilitySkillsRequest
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.EmployabilitySkillRating as DomainEmployabilitySkillRating
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.EmployabilitySkillType as DomainEmployabilitySkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillRating as ApiEmployabilitySkillRating
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillType as ApiEmployabilitySkillType

@Component
class EmployabilitySkillsResourceMapper {

  fun fromModelToDto(
    prisonNumber: String,
    request: CreateEmployabilitySkillsRequest,
  ): CreateEmployabilitySkillsDto {
    val employabilitySkills = request.employabilitySkills.map { fromModelToDto(prisonNumber, it) }
    return CreateEmployabilitySkillsDto(employabilitySkills)
  }

  fun fromModelToDto(
    prisonNumber: String,
    request: CreateEmployabilitySkillRequest,
  ): EmployabilitySkillDto = EmployabilitySkillDto(
    prisonNumber = prisonNumber,
    prisonId = request.prisonId,
    employabilitySkillType = request.employabilitySkillType.toDomain(),
    employabilitySkillRating = request.employabilitySkillRating.toDomain(),
    activityName = request.activityName,
    evidence = request.evidence,
    conversationDate = request.conversationDate,
  )

  fun fromDtoToModel(skill: EmployabilitySkillDto): CreateEmployabilitySkillRequest = CreateEmployabilitySkillRequest(
    prisonId = skill.prisonId,
    employabilitySkillType = skill.employabilitySkillType.toApi(),
    employabilitySkillRating = skill.employabilitySkillRating.toApi(),
    activityName = skill.activityName,
    evidence = skill.evidence,
    conversationDate = skill.conversationDate,
  )

  /**
   * Map domain DTO -> API model (bulk).
   */
  fun fromDtoToModel(skills: List<EmployabilitySkillDto>): List<CreateEmployabilitySkillRequest> = skills.map { fromDtoToModel(it) }
}

private fun ApiEmployabilitySkillType.toDomain(): DomainEmployabilitySkillType = DomainEmployabilitySkillType.valueOf(this.name)

private fun ApiEmployabilitySkillRating.toDomain(): DomainEmployabilitySkillRating = DomainEmployabilitySkillRating.valueOf(this.name)

private fun DomainEmployabilitySkillType.toApi(): ApiEmployabilitySkillType = ApiEmployabilitySkillType.valueOf(this.name)

private fun DomainEmployabilitySkillRating.toApi(): ApiEmployabilitySkillRating = ApiEmployabilitySkillRating.valueOf(this.name)
