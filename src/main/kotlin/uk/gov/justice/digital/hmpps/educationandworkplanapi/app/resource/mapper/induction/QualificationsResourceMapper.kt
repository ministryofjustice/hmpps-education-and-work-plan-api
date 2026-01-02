package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.PreviousQualifications
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.Qualification
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.CreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.UpdateOrCreateQualificationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.UpdateOrCreateQualificationDto.CreateQualificationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.UpdateOrCreateQualificationDto.UpdateQualificationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.UpdatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AchievedQualificationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateOrUpdateAchievedQualificationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousQualificationsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePreviousQualificationsRequest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationLevel as EducationLevelDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.QualificationLevel as QualificationLevelDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationLevel as EducationLevelApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.QualificationLevel as QualificationLevelApi

@Component
class QualificationsResourceMapper(
  private val instantMapper: InstantMapper,
  private val userService: ManageUserService,
) {

  fun toCreatePreviousQualificationsDto(
    request: CreatePreviousQualificationsRequest,
    prisonNumber: String,
    prisonId: String,
  ): CreatePreviousQualificationsDto = with(request) {
    CreatePreviousQualificationsDto(
      prisonNumber = prisonNumber,
      prisonId = prisonId,
      educationLevel = educationLevel?.let { toEducationLevel(it) } ?: EducationLevelDomain.NOT_SURE,
      qualifications = qualifications?.let { toUpdateOrCreateQualificationDtos(it, prisonId) } ?: emptyList(),
    )
  }

  fun toPreviousQualificationsResponse(previousQualifications: PreviousQualifications): PreviousQualificationsResponse = with(previousQualifications) {
    PreviousQualificationsResponse(
      reference = reference,
      qualifications = toAchievedQualificationResponses(qualifications),
      educationLevel = toEducationLevel(educationLevel),
      createdBy = createdBy,
      createdByDisplayName = userService.getUserDetails(createdBy).name,
      createdAt = instantMapper.toOffsetDateTime(createdAt)!!,
      createdAtPrison = createdAtPrison,
      updatedBy = lastUpdatedBy,
      updatedByDisplayName = userService.getUserDetails(lastUpdatedBy).name,
      updatedAt = instantMapper.toOffsetDateTime(lastUpdatedAt)!!,
      updatedAtPrison = lastUpdatedAtPrison,
    )
  }

  fun toUpdatePreviousQualificationsDto(
    request: UpdatePreviousQualificationsRequest,
    prisonId: String,
    prisonNumber: String,
  ): UpdatePreviousQualificationsDto = with(request) {
    UpdatePreviousQualificationsDto(
      reference = reference,
      prisonNumber = prisonNumber,
      prisonId = prisonId,
      educationLevel = toEducationLevel(request.educationLevel),
      qualifications = toUpdateOrCreateQualificationDtos(request.qualifications ?: emptyList(), prisonId),
    )
  }

  fun toUpdateOrCreateQualificationDto(
    achievedQualification: CreateOrUpdateAchievedQualificationRequest,
    prisonId: String,
  ): UpdateOrCreateQualificationDto = if (achievedQualification.reference == null) {
    CreateQualificationDto(
      prisonId = prisonId,
      subject = achievedQualification.subject,
      level = toQualificationLevel(achievedQualification.level),
      grade = achievedQualification.grade,
    )
  } else {
    UpdateQualificationDto(
      prisonId = prisonId,
      reference = achievedQualification.reference,
      subject = achievedQualification.subject,
      level = toQualificationLevel(achievedQualification.level),
      grade = achievedQualification.grade,
    )
  }

  fun toUpdateOrCreateQualificationDtos(
    achievedQualifications: List<CreateOrUpdateAchievedQualificationRequest>,
    prisonId: String,
  ): List<UpdateOrCreateQualificationDto> = achievedQualifications.map { toUpdateOrCreateQualificationDto(it, prisonId) }

  fun toAchievedQualificationResponse(qualification: Qualification): AchievedQualificationResponse = AchievedQualificationResponse(
    reference = qualification.reference,
    subject = qualification.subject,
    level = toQualificationLevel(qualification.level),
    grade = qualification.grade,
    createdBy = qualification.createdBy,
    createdAt = instantMapper.toOffsetDateTime(qualification.createdAt)!!,
    createdAtPrison = qualification.createdAtPrison,
    updatedBy = qualification.lastUpdatedBy,
    updatedAt = instantMapper.toOffsetDateTime(qualification.lastUpdatedAt)!!,
    updatedAtPrison = qualification.lastUpdatedAtPrison,
  )

  fun toAchievedQualificationResponses(qualifications: List<Qualification>): List<AchievedQualificationResponse> = qualifications.map { toAchievedQualificationResponse(it) }

  fun toEducationLevel(educationLevel: EducationLevelApi?): EducationLevelDomain? = when (educationLevel) {
    EducationLevelApi.NOT_SURE -> EducationLevelDomain.NOT_SURE
    EducationLevelApi.NO_FORMAL_EDUCATION -> EducationLevelDomain.NO_FORMAL_EDUCATION
    EducationLevelApi.PRIMARY_SCHOOL -> EducationLevelDomain.PRIMARY_SCHOOL
    EducationLevelApi.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS -> EducationLevelDomain.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS
    EducationLevelApi.SECONDARY_SCHOOL_TOOK_EXAMS -> EducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS
    EducationLevelApi.FURTHER_EDUCATION_COLLEGE -> EducationLevelDomain.FURTHER_EDUCATION_COLLEGE
    EducationLevelApi.UNDERGRADUATE_DEGREE_AT_UNIVERSITY -> EducationLevelDomain.UNDERGRADUATE_DEGREE_AT_UNIVERSITY
    EducationLevelApi.POSTGRADUATE_DEGREE_AT_UNIVERSITY -> EducationLevelDomain.POSTGRADUATE_DEGREE_AT_UNIVERSITY
    null -> null
  }

  fun toEducationLevel(educationLevel: EducationLevelDomain): EducationLevelApi = when (educationLevel) {
    EducationLevelDomain.NOT_SURE -> EducationLevelApi.NOT_SURE
    EducationLevelDomain.NO_FORMAL_EDUCATION -> EducationLevelApi.NO_FORMAL_EDUCATION
    EducationLevelDomain.PRIMARY_SCHOOL -> EducationLevelApi.PRIMARY_SCHOOL
    EducationLevelDomain.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS -> EducationLevelApi.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS
    EducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS -> EducationLevelApi.SECONDARY_SCHOOL_TOOK_EXAMS
    EducationLevelDomain.FURTHER_EDUCATION_COLLEGE -> EducationLevelApi.FURTHER_EDUCATION_COLLEGE
    EducationLevelDomain.UNDERGRADUATE_DEGREE_AT_UNIVERSITY -> EducationLevelApi.UNDERGRADUATE_DEGREE_AT_UNIVERSITY
    EducationLevelDomain.POSTGRADUATE_DEGREE_AT_UNIVERSITY -> EducationLevelApi.POSTGRADUATE_DEGREE_AT_UNIVERSITY
  }

  fun toQualificationLevel(qualificationLevel: QualificationLevelApi): QualificationLevelDomain = when (qualificationLevel) {
    QualificationLevelApi.ENTRY_LEVEL -> QualificationLevelDomain.ENTRY_LEVEL
    QualificationLevelApi.LEVEL_1 -> QualificationLevelDomain.LEVEL_1
    QualificationLevelApi.LEVEL_2 -> QualificationLevelDomain.LEVEL_2
    QualificationLevelApi.LEVEL_3 -> QualificationLevelDomain.LEVEL_3
    QualificationLevelApi.LEVEL_4 -> QualificationLevelDomain.LEVEL_4
    QualificationLevelApi.LEVEL_5 -> QualificationLevelDomain.LEVEL_5
    QualificationLevelApi.LEVEL_6 -> QualificationLevelDomain.LEVEL_6
    QualificationLevelApi.LEVEL_7 -> QualificationLevelDomain.LEVEL_7
    QualificationLevelApi.LEVEL_8 -> QualificationLevelDomain.LEVEL_8
  }

  fun toQualificationLevel(qualificationLevel: QualificationLevelDomain): QualificationLevelApi = when (qualificationLevel) {
    QualificationLevelDomain.ENTRY_LEVEL -> QualificationLevelApi.ENTRY_LEVEL
    QualificationLevelDomain.LEVEL_1 -> QualificationLevelApi.LEVEL_1
    QualificationLevelDomain.LEVEL_2 -> QualificationLevelApi.LEVEL_2
    QualificationLevelDomain.LEVEL_3 -> QualificationLevelApi.LEVEL_3
    QualificationLevelDomain.LEVEL_4 -> QualificationLevelApi.LEVEL_4
    QualificationLevelDomain.LEVEL_5 -> QualificationLevelApi.LEVEL_5
    QualificationLevelDomain.LEVEL_6 -> QualificationLevelApi.LEVEL_6
    QualificationLevelDomain.LEVEL_7 -> QualificationLevelApi.LEVEL_7
    QualificationLevelDomain.LEVEL_8 -> QualificationLevelApi.LEVEL_8
  }
}
