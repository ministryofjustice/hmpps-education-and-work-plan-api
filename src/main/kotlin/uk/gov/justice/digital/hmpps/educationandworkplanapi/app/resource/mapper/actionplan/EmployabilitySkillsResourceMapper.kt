package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.EmployabilitySkill
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.EmployabilitySkillRating
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.EmployabilitySkillSessionType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.EmployabilitySkillType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.dto.CreateEmployabilitySkillsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.dto.EmployabilitySkillDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateEmployabilitySkillRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateEmployabilitySkillsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GetEmployabilitySkillsResponse
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.dto.EmployabilitySkillRating as DomainEmployabilitySkillRating
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.dto.EmployabilitySkillSessionType as DomainEmployabilitySkillSessionType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.dto.EmployabilitySkillType as DomainEmployabilitySkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillRating as ApiEmployabilitySkillRating
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillSessionType as ApiEmployabilitySkillSessionType
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
      sessionType = sessionType?.toDomain(),
      sessionTypeDescription = request.sessionTypeDescription,
      evidence = request.evidence,
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
      employabilitySkillType = employabilitySkillType.toApi(),
      employabilitySkillRating = rating.toApi(),
      sessionType = sessionType?.toApi(),
      sessionTypeDescription = sessionTypeDescription,
      evidence = evidence,

    )
  }
}

private fun EmployabilitySkillType.toApi(): ApiEmployabilitySkillType = when (this) {
  EmployabilitySkillType.TEAMWORK -> ApiEmployabilitySkillType.TEAMWORK
  EmployabilitySkillType.TIMEKEEPING -> ApiEmployabilitySkillType.TIMEKEEPING
  EmployabilitySkillType.COMMUNICATION -> ApiEmployabilitySkillType.COMMUNICATION
  EmployabilitySkillType.PLANNING -> ApiEmployabilitySkillType.PLANNING
  EmployabilitySkillType.ORGANISATION -> ApiEmployabilitySkillType.ORGANISATION
  EmployabilitySkillType.PROBLEM_SOLVING -> ApiEmployabilitySkillType.PROBLEM_SOLVING
  EmployabilitySkillType.INITIATIVE -> ApiEmployabilitySkillType.INITIATIVE
  EmployabilitySkillType.ADAPTABILITY -> ApiEmployabilitySkillType.ADAPTABILITY
  EmployabilitySkillType.RELIABILITY -> ApiEmployabilitySkillType.RELIABILITY
  EmployabilitySkillType.CREATIVITY -> ApiEmployabilitySkillType.CREATIVITY
}

private fun EmployabilitySkillRating.toApi(): ApiEmployabilitySkillRating = when (this) {
  EmployabilitySkillRating.NOT_CONFIDENT -> ApiEmployabilitySkillRating.NOT_CONFIDENT
  EmployabilitySkillRating.LITTLE_CONFIDENCE -> ApiEmployabilitySkillRating.LITTLE_CONFIDENCE
  EmployabilitySkillRating.QUITE_CONFIDENT -> ApiEmployabilitySkillRating.QUITE_CONFIDENT
  EmployabilitySkillRating.VERY_CONFIDENT -> ApiEmployabilitySkillRating.VERY_CONFIDENT
}

private fun EmployabilitySkillSessionType.toApi(): ApiEmployabilitySkillSessionType = when (this) {
  EmployabilitySkillSessionType.CIAG_INDUCTION -> ApiEmployabilitySkillSessionType.CIAG_INDUCTION
  EmployabilitySkillSessionType.CIAG_REVIEW -> ApiEmployabilitySkillSessionType.CIAG_REVIEW
  EmployabilitySkillSessionType.EDUCATION_REVIEW -> ApiEmployabilitySkillSessionType.EDUCATION_REVIEW
  EmployabilitySkillSessionType.INDUSTRIES_REVIEW -> ApiEmployabilitySkillSessionType.INDUSTRIES_REVIEW
}

private fun ApiEmployabilitySkillType.toDomain(): DomainEmployabilitySkillType = when (this) {
  ApiEmployabilitySkillType.TEAMWORK -> DomainEmployabilitySkillType.TEAMWORK
  ApiEmployabilitySkillType.TIMEKEEPING -> DomainEmployabilitySkillType.TIMEKEEPING
  ApiEmployabilitySkillType.COMMUNICATION -> DomainEmployabilitySkillType.COMMUNICATION
  ApiEmployabilitySkillType.PLANNING -> DomainEmployabilitySkillType.PLANNING
  ApiEmployabilitySkillType.ORGANISATION -> DomainEmployabilitySkillType.ORGANISATION
  ApiEmployabilitySkillType.PROBLEM_SOLVING -> DomainEmployabilitySkillType.PROBLEM_SOLVING
  ApiEmployabilitySkillType.INITIATIVE -> DomainEmployabilitySkillType.INITIATIVE
  ApiEmployabilitySkillType.ADAPTABILITY -> DomainEmployabilitySkillType.ADAPTABILITY
  ApiEmployabilitySkillType.RELIABILITY -> DomainEmployabilitySkillType.RELIABILITY
  ApiEmployabilitySkillType.CREATIVITY -> DomainEmployabilitySkillType.CREATIVITY
}

private fun ApiEmployabilitySkillSessionType.toDomain(): DomainEmployabilitySkillSessionType = when (this) {
  ApiEmployabilitySkillSessionType.CIAG_INDUCTION -> DomainEmployabilitySkillSessionType.CIAG_INDUCTION
  ApiEmployabilitySkillSessionType.CIAG_REVIEW -> DomainEmployabilitySkillSessionType.CIAG_REVIEW
  ApiEmployabilitySkillSessionType.EDUCATION_REVIEW -> DomainEmployabilitySkillSessionType.EDUCATION_REVIEW
  ApiEmployabilitySkillSessionType.INDUSTRIES_REVIEW -> DomainEmployabilitySkillSessionType.INDUSTRIES_REVIEW
}

private fun ApiEmployabilitySkillRating.toDomain(): DomainEmployabilitySkillRating = when (this) {
  ApiEmployabilitySkillRating.NOT_CONFIDENT -> DomainEmployabilitySkillRating.NOT_CONFIDENT
  ApiEmployabilitySkillRating.LITTLE_CONFIDENCE -> DomainEmployabilitySkillRating.LITTLE_CONFIDENCE
  ApiEmployabilitySkillRating.QUITE_CONFIDENT -> DomainEmployabilitySkillRating.QUITE_CONFIDENT
  ApiEmployabilitySkillRating.VERY_CONFIDENT -> DomainEmployabilitySkillRating.VERY_CONFIDENT
}
