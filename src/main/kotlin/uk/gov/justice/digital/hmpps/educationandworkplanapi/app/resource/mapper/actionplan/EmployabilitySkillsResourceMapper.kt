package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.personallearningplan.EmployabilitySkill
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateEmployabilitySkillsDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.EmployabilitySkillDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateEmployabilitySkillRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateEmployabilitySkillsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GetEmployabilitySkillsResponse
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.EmployabilitySkillRating as DomainEmployabilitySkillRating
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.EmployabilitySkillType as DomainEmployabilitySkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillRating as ApiEmployabilitySkillRating
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillType as ApiEmployabilitySkillType

@Component
class EmployabilitySkillsResourceMapper(
  private val instantMapper: InstantMapper,
  private val userService: ManageUserService,
) {

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
  ): EmployabilitySkillDto = with(request) {
    EmployabilitySkillDto(
      prisonNumber = prisonNumber,
      prisonId = prisonId,
      employabilitySkillType = employabilitySkillType.toDomain(),
      employabilitySkillRating = employabilitySkillRating.toDomain(),
      activityName = request.activityName,
      evidence = request.evidence,
      conversationDate = request.conversationDate,
    )
  }

  fun fromModelToResponse(employabilitySkill: EmployabilitySkill): GetEmployabilitySkillsResponse = with(employabilitySkill) {
    GetEmployabilitySkillsResponse(
      createdBy = createdBy,
      createdByDisplayName = userService.getUserDetails(createdBy).name,
      createdAt = instantMapper.toOffsetDateTime(createdAt)!!,
      createdAtPrison = createdAtPrison,
      updatedByDisplayName = userService.getUserDetails(updatedBy).name,
      updatedAt = instantMapper.toOffsetDateTime(updatedAt)!!,
      updatedBy = updatedBy,
      updatedAtPrison = updatedAtPrison,
      employabilitySkillType = ApiEmployabilitySkillType.valueOf(employabilitySkillType.name),
      employabilitySkillRating = ApiEmployabilitySkillRating.valueOf(ratingCode),
      activityName = activityName,
      evidence = evidence,
      conversationDate = conversationDate,
    )
  }
}

private fun ApiEmployabilitySkillType.toDomain(): DomainEmployabilitySkillType = DomainEmployabilitySkillType.valueOf(this.name)
private fun ApiEmployabilitySkillRating.toDomain(): DomainEmployabilitySkillRating = DomainEmployabilitySkillRating.valueOf(this.name)
