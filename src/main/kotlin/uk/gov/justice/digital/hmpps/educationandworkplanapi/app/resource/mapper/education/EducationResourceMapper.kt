package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.education

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.PreviousQualifications
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.Qualification
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.CreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.UpdateOrCreateQualificationDto.CreateQualificationDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AchievedQualificationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateAchievedQualificationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateEducationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationResponse
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationLevel as EducationLevelDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.QualificationLevel as QualificationLevelDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationLevel as EducationLevelApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.QualificationLevel as QualificationLevelApi

@Component
class EducationResourceMapper(private val instantMapper: InstantMapper) {

  fun toEducationResponse(previousQualifications: PreviousQualifications): EducationResponse =
    EducationResponse(
      reference = previousQualifications.reference,
      createdAt = instantMapper.toOffsetDateTime(previousQualifications.createdAt)!!,
      createdAtPrison = previousQualifications.createdAtPrison,
      createdBy = previousQualifications.createdBy!!,
      createdByDisplayName = previousQualifications.createdByDisplayName!!,
      updatedAt = instantMapper.toOffsetDateTime(previousQualifications.lastUpdatedAt)!!,
      updatedAtPrison = previousQualifications.lastUpdatedAtPrison,
      updatedBy = previousQualifications.lastUpdatedBy!!,
      updatedByDisplayName = previousQualifications.lastUpdatedByDisplayName!!,
      educationLevel = toEducationLevel(previousQualifications.educationLevel),
      qualifications = toAchievedQualificationResponses(previousQualifications.qualifications),
    )

  fun toCreatePreviousQualificationsDto(prisonNumber: String, request: CreateEducationRequest): CreatePreviousQualificationsDto =
    CreatePreviousQualificationsDto(
      prisonNumber = prisonNumber,
      educationLevel = toEducationLevel(request.educationLevel),
      qualifications = toCreateQualificationDtos(request.qualifications, request.prisonId),
      prisonId = request.prisonId,
    )

  private fun toAchievedQualificationResponse(qualification: Qualification): AchievedQualificationResponse =
    AchievedQualificationResponse(
      reference = qualification.reference,
      createdAt = instantMapper.toOffsetDateTime(qualification.createdAt)!!,
      createdBy = qualification.createdBy,
      createdAtPrison = qualification.createdAtPrison,
      updatedAt = instantMapper.toOffsetDateTime(qualification.lastUpdatedAt)!!,
      updatedBy = qualification.lastUpdatedBy,
      updatedAtPrison = qualification.lastUpdatedAtPrison,
      subject = qualification.subject,
      level = toQualificationLevel(qualification.level),
      grade = qualification.grade,
    )

  private fun toAchievedQualificationResponses(qualifications: List<Qualification>): List<AchievedQualificationResponse> =
    qualifications.map { toAchievedQualificationResponse(it) }

  private fun toCreateQualificationDto(createAchievedQualificationRequest: CreateAchievedQualificationRequest, prisonId: String): CreateQualificationDto =
    with(createAchievedQualificationRequest) {
      CreateQualificationDto(
        subject = subject,
        level = toQualificationLevel(level),
        grade = grade,
        prisonId = prisonId,
      )
    }

  private fun toCreateQualificationDtos(createAchievedQualificationRequests: List<CreateAchievedQualificationRequest>, prisonId: String): List<CreateQualificationDto> =
    createAchievedQualificationRequests.map { toCreateQualificationDto(it, prisonId) }

  private fun toEducationLevel(educationLevel: EducationLevelDomain): EducationLevelApi =
    when (educationLevel) {
      EducationLevelDomain.NOT_SURE -> EducationLevelApi.NOT_SURE
      EducationLevelDomain.PRIMARY_SCHOOL -> EducationLevelApi.PRIMARY_SCHOOL
      EducationLevelDomain.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS -> EducationLevelApi.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS
      EducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS -> EducationLevelApi.SECONDARY_SCHOOL_TOOK_EXAMS
      EducationLevelDomain.FURTHER_EDUCATION_COLLEGE -> EducationLevelApi.FURTHER_EDUCATION_COLLEGE
      EducationLevelDomain.UNDERGRADUATE_DEGREE_AT_UNIVERSITY -> EducationLevelApi.UNDERGRADUATE_DEGREE_AT_UNIVERSITY
      EducationLevelDomain.POSTGRADUATE_DEGREE_AT_UNIVERSITY -> EducationLevelApi.POSTGRADUATE_DEGREE_AT_UNIVERSITY
    }

  private fun toEducationLevel(educationLevel: EducationLevelApi): EducationLevelDomain =
    when (educationLevel) {
      EducationLevelApi.NOT_SURE -> EducationLevelDomain.NOT_SURE
      EducationLevelApi.PRIMARY_SCHOOL -> EducationLevelDomain.PRIMARY_SCHOOL
      EducationLevelApi.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS -> EducationLevelDomain.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS
      EducationLevelApi.SECONDARY_SCHOOL_TOOK_EXAMS -> EducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS
      EducationLevelApi.FURTHER_EDUCATION_COLLEGE -> EducationLevelDomain.FURTHER_EDUCATION_COLLEGE
      EducationLevelApi.UNDERGRADUATE_DEGREE_AT_UNIVERSITY -> EducationLevelDomain.UNDERGRADUATE_DEGREE_AT_UNIVERSITY
      EducationLevelApi.POSTGRADUATE_DEGREE_AT_UNIVERSITY -> EducationLevelDomain.POSTGRADUATE_DEGREE_AT_UNIVERSITY
    }

  private fun toQualificationLevel(qualificationLevel: QualificationLevelDomain): QualificationLevelApi =
    when (qualificationLevel) {
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

  private fun toQualificationLevel(qualificationLevel: QualificationLevelApi): QualificationLevelDomain =
    when (qualificationLevel) {
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
}
