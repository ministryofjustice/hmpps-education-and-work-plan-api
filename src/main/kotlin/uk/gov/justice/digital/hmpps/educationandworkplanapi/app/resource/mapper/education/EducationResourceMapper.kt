package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.education

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.PreviousQualifications
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.Qualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AchievedQualificationResponse
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
      educationLevel = previousQualifications.educationLevel?.let { toEducationLevel(it) } ?: EducationLevelApi.NOT_SURE,
      qualifications = toAchievedQualificationResponses(previousQualifications.qualifications),
    )

  fun toAchievedQualificationResponse(qualification: Qualification): AchievedQualificationResponse =
    AchievedQualificationResponse(
      reference = qualification.reference!!,
      createdAt = instantMapper.toOffsetDateTime(qualification.createdAt)!!,
      createdBy = qualification.createdBy!!,
      updatedAt = instantMapper.toOffsetDateTime(qualification.lastUpdatedAt)!!,
      updatedBy = qualification.lastUpdatedBy!!,
      subject = qualification.subject,
      level = toQualificationLevel(qualification.level),
      grade = qualification.grade,
    )

  fun toAchievedQualificationResponses(qualifications: List<Qualification>): List<AchievedQualificationResponse> =
    qualifications.map { toAchievedQualificationResponse(it) }

  fun toEducationLevel(educationLevel: EducationLevelDomain): EducationLevelApi =
    when (educationLevel) {
      EducationLevelDomain.NOT_SURE -> EducationLevelApi.NOT_SURE
      EducationLevelDomain.PRIMARY_SCHOOL -> EducationLevelApi.PRIMARY_SCHOOL
      EducationLevelDomain.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS -> EducationLevelApi.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS
      EducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS -> EducationLevelApi.SECONDARY_SCHOOL_TOOK_EXAMS
      EducationLevelDomain.FURTHER_EDUCATION_COLLEGE -> EducationLevelApi.FURTHER_EDUCATION_COLLEGE
      EducationLevelDomain.UNDERGRADUATE_DEGREE_AT_UNIVERSITY -> EducationLevelApi.UNDERGRADUATE_DEGREE_AT_UNIVERSITY
      EducationLevelDomain.POSTGRADUATE_DEGREE_AT_UNIVERSITY -> EducationLevelApi.POSTGRADUATE_DEGREE_AT_UNIVERSITY
    }

  fun toQualificationLevel(qualificationLevel: QualificationLevelDomain): QualificationLevelApi =
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
}
