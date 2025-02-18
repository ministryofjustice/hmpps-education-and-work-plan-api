package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.PreviousWorkExperiences
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkExperience
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkExperienceType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousWorkExperiencesRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousWorkExperience
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousWorkExperiencesResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePreviousWorkExperiencesRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HasWorkedBefore as HasWorkedBeforeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HasWorkedBefore as HasWorkedBeforeApi

@Component
class WorkExperiencesResourceMapper(
  private val instantMapper: InstantMapper,
  private val userService: ManageUserService,
) {
  fun toCreatePreviousWorkExperiencesDto(request: CreatePreviousWorkExperiencesRequest, prisonId: String): CreatePreviousWorkExperiencesDto = with(request) {
    CreatePreviousWorkExperiencesDto(
      prisonId = prisonId,
      experiences = experiences?.map { toWorkExperience(it) }.orEmpty(),
      hasWorkedBefore = toHasWorkedBefore(hasWorkedBefore),
      hasWorkedBeforeNotRelevantReason = hasWorkedBeforeNotRelevantReason,
    )
  }

  fun toPreviousWorkExperiencesResponse(workExperiences: PreviousWorkExperiences): PreviousWorkExperiencesResponse = with(workExperiences) {
    PreviousWorkExperiencesResponse(
      reference = reference,
      experiences = experiences.map { toPreviousWorkExperience(it) },
      hasWorkedBefore = toHasWorkedBefore(hasWorkedBefore),
      hasWorkedBeforeNotRelevantReason = hasWorkedBeforeNotRelevantReason,
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

  fun toUpdatePreviousWorkExperiencesDto(request: UpdatePreviousWorkExperiencesRequest, prisonId: String): UpdatePreviousWorkExperiencesDto = with(request) {
    UpdatePreviousWorkExperiencesDto(
      reference = reference,
      prisonId = prisonId,
      experiences = experiences?.map { toWorkExperience(it) }.orEmpty(),
      hasWorkedBefore = toHasWorkedBefore(hasWorkedBefore),
      hasWorkedBeforeNotRelevantReason = hasWorkedBeforeNotRelevantReason,
    )
  }

  private fun toWorkExperience(experience: PreviousWorkExperience): WorkExperience = with(experience) {
    WorkExperience(
      role = role,
      details = details,
      experienceType = toWorkExperienceType(experienceType),
      experienceTypeOther = experienceTypeOther,
    )
  }

  private fun toPreviousWorkExperience(experience: WorkExperience): PreviousWorkExperience = with(experience) {
    PreviousWorkExperience(
      role = role,
      details = details,
      experienceType = toWorkType(experienceType),
      experienceTypeOther = experienceTypeOther,
    )
  }

  private fun toWorkType(experienceType: WorkExperienceType): WorkType = when (experienceType) {
    WorkExperienceType.OUTDOOR -> WorkType.OUTDOOR
    WorkExperienceType.CONSTRUCTION -> WorkType.CONSTRUCTION
    WorkExperienceType.DRIVING -> WorkType.DRIVING
    WorkExperienceType.BEAUTY -> WorkType.BEAUTY
    WorkExperienceType.HOSPITALITY -> WorkType.HOSPITALITY
    WorkExperienceType.TECHNICAL -> WorkType.TECHNICAL
    WorkExperienceType.MANUFACTURING -> WorkType.MANUFACTURING
    WorkExperienceType.OFFICE -> WorkType.OFFICE
    WorkExperienceType.RETAIL -> WorkType.RETAIL
    WorkExperienceType.SPORTS -> WorkType.SPORTS
    WorkExperienceType.WAREHOUSING -> WorkType.WAREHOUSING
    WorkExperienceType.WASTE_MANAGEMENT -> WorkType.WASTE_MANAGEMENT
    WorkExperienceType.EDUCATION_TRAINING -> WorkType.EDUCATION_TRAINING
    WorkExperienceType.CLEANING_AND_MAINTENANCE -> WorkType.CLEANING_AND_MAINTENANCE
    WorkExperienceType.OTHER -> WorkType.OTHER
  }

  private fun toWorkExperienceType(experienceType: WorkType): WorkExperienceType = when (experienceType) {
    WorkType.OUTDOOR -> WorkExperienceType.OUTDOOR
    WorkType.CONSTRUCTION -> WorkExperienceType.CONSTRUCTION
    WorkType.DRIVING -> WorkExperienceType.DRIVING
    WorkType.BEAUTY -> WorkExperienceType.BEAUTY
    WorkType.HOSPITALITY -> WorkExperienceType.HOSPITALITY
    WorkType.TECHNICAL -> WorkExperienceType.TECHNICAL
    WorkType.MANUFACTURING -> WorkExperienceType.MANUFACTURING
    WorkType.OFFICE -> WorkExperienceType.OFFICE
    WorkType.RETAIL -> WorkExperienceType.RETAIL
    WorkType.SPORTS -> WorkExperienceType.SPORTS
    WorkType.WAREHOUSING -> WorkExperienceType.WAREHOUSING
    WorkType.WASTE_MANAGEMENT -> WorkExperienceType.WASTE_MANAGEMENT
    WorkType.EDUCATION_TRAINING -> WorkExperienceType.EDUCATION_TRAINING
    WorkType.CLEANING_AND_MAINTENANCE -> WorkExperienceType.CLEANING_AND_MAINTENANCE
    WorkType.OTHER -> WorkExperienceType.OTHER
  }

  private fun toHasWorkedBefore(hasWorkedBefore: HasWorkedBeforeApi): HasWorkedBeforeDomain = when (hasWorkedBefore) {
    HasWorkedBeforeApi.YES -> HasWorkedBeforeDomain.YES
    HasWorkedBeforeApi.NO -> HasWorkedBeforeDomain.NO
    HasWorkedBeforeApi.NOT_RELEVANT -> HasWorkedBeforeDomain.NOT_RELEVANT
  }

  private fun toHasWorkedBefore(hasWorkedBefore: HasWorkedBeforeDomain): HasWorkedBeforeApi = when (hasWorkedBefore) {
    HasWorkedBeforeDomain.YES -> HasWorkedBeforeApi.YES
    HasWorkedBeforeDomain.NO -> HasWorkedBeforeApi.NO
    HasWorkedBeforeDomain.NOT_RELEVANT -> HasWorkedBeforeApi.NOT_RELEVANT
  }
}
