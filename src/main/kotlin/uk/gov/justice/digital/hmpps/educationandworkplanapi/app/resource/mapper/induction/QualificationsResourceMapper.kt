package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.NullValueMappingStrategy
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.PreviousQualifications
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.Qualification
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.CreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.UpdateOrCreateQualificationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.UpdateOrCreateQualificationDto.CreateQualificationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.UpdateOrCreateQualificationDto.UpdateQualificationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.UpdatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AchievedQualificationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateOrUpdateAchievedQualificationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousQualificationsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePreviousQualificationsRequest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationLevel as EducationLevelDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.QualificationLevel as QualificationLevelDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationLevel as EducationLevelApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.QualificationLevel as QualificationLevelApi

@Mapper(
  uses = [
    InstantMapper::class,
  ],
  nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
)
abstract class QualificationsResourceMapper {

  @Autowired
  private lateinit var instantMapper: InstantMapper

  fun toCreatePreviousQualificationsDto(request: CreatePreviousQualificationsRequest, prisonNumber: String, prisonId: String): CreatePreviousQualificationsDto =
    CreatePreviousQualificationsDto(
      prisonNumber = prisonNumber,
      prisonId = prisonId,
      educationLevel = request.educationLevel?.let { toEducationLevel(it) } ?: EducationLevelDomain.NOT_SURE,
      qualifications = request.qualifications?.let { toUpdateOrCreateQualificationDtos(it) } ?: emptyList(),
    )

  @Mapping(target = "updatedBy", source = "lastUpdatedBy")
  @Mapping(target = "updatedByDisplayName", source = "lastUpdatedByDisplayName")
  @Mapping(target = "updatedAt", source = "lastUpdatedAt")
  @Mapping(target = "updatedAtPrison", source = "lastUpdatedAtPrison")
  abstract fun toPreviousQualificationsResponse(previousQualifications: PreviousQualifications?): PreviousQualificationsResponse?

  abstract fun toUpdatePreviousQualificationsDto(request: UpdatePreviousQualificationsRequest, prisonId: String): UpdatePreviousQualificationsDto

  fun toUpdateOrCreateQualificationDto(achievedQualification: CreateOrUpdateAchievedQualificationRequest): UpdateOrCreateQualificationDto =
    if (achievedQualification.reference == null) {
      CreateQualificationDto(
        subject = achievedQualification.subject,
        level = toQualificationLevel(achievedQualification.level),
        grade = achievedQualification.grade,
      )
    } else {
      UpdateQualificationDto(
        reference = achievedQualification.reference!!,
        subject = achievedQualification.subject,
        level = toQualificationLevel(achievedQualification.level),
        grade = achievedQualification.grade,
      )
    }

  fun toUpdateOrCreateQualificationDtos(achievedQualifications: List<CreateOrUpdateAchievedQualificationRequest>): List<UpdateOrCreateQualificationDto> =
    achievedQualifications.map { toUpdateOrCreateQualificationDto(it) }

  fun toAchievedQualificationResponse(qualification: Qualification): AchievedQualificationResponse =
    AchievedQualificationResponse(
      reference = qualification.reference,
      subject = qualification.subject,
      level = toQualificationLevel(qualification.level),
      grade = qualification.grade,
      createdBy = qualification.createdBy,
      createdAt = instantMapper.toOffsetDateTime(qualification.createdAt)!!,
      updatedBy = qualification.lastUpdatedBy,
      updatedAt = instantMapper.toOffsetDateTime(qualification.lastUpdatedAt)!!,
    )

  fun toAchievedQualificationResponses(qualifications: List<Qualification>): List<AchievedQualificationResponse> =
    qualifications.map { toAchievedQualificationResponse(it) }

  fun toEducationLevel(educationLevel: EducationLevelApi?): EducationLevelDomain? =
    when (educationLevel) {
      EducationLevelApi.NOT_SURE -> EducationLevelDomain.NOT_SURE
      EducationLevelApi.PRIMARY_SCHOOL -> EducationLevelDomain.PRIMARY_SCHOOL
      EducationLevelApi.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS -> EducationLevelDomain.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS
      EducationLevelApi.SECONDARY_SCHOOL_TOOK_EXAMS -> EducationLevelDomain.SECONDARY_SCHOOL_TOOK_EXAMS
      EducationLevelApi.FURTHER_EDUCATION_COLLEGE -> EducationLevelDomain.FURTHER_EDUCATION_COLLEGE
      EducationLevelApi.UNDERGRADUATE_DEGREE_AT_UNIVERSITY -> EducationLevelDomain.UNDERGRADUATE_DEGREE_AT_UNIVERSITY
      EducationLevelApi.POSTGRADUATE_DEGREE_AT_UNIVERSITY -> EducationLevelDomain.POSTGRADUATE_DEGREE_AT_UNIVERSITY
      null -> null
    }

  fun toQualificationLevel(qualificationLevel: QualificationLevelApi): QualificationLevelDomain =
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
