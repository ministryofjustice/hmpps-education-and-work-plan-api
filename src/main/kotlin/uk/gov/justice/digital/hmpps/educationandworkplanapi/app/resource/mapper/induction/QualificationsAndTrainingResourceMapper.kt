package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.NullValueMappingStrategy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PreviousQualifications
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PreviousTraining
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.Qualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.TrainingType.OTHER
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.UpdatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.UpdatePreviousTrainingDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateEducationAndQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationAndQualificationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateEducationAndQualificationsRequest
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.HighestEducationLevel as HighestEducationLevelDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.TrainingType as TrainingTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HighestEducationLevel as HighestEducationLevelApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TrainingType as TrainingTypeApi

@Mapper(nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
abstract class QualificationsAndTrainingResourceMapper {

  fun toCreatePreviousQualificationsDto(
    request: CreateEducationAndQualificationsRequest?,
    prisonId: String,
  ): CreatePreviousQualificationsDto? {
    if (request == null) {
      return null
    }

    return CreatePreviousQualificationsDto(
      educationLevel = toHighestEducationLevelDomain(request.educationLevel) ?: HighestEducationLevelDomain.NOT_SURE,
      qualifications = request.qualifications?.map { toQualification(it) } ?: emptyList(),
      prisonId = prisonId,
    )
  }

  abstract fun toQualification(qualificationRequest: AchievedQualification): Qualification

  @Mapping(target = "trainingTypes", source = "request.additionalTraining")
  @Mapping(target = "trainingTypeOther", source = "request.additionalTrainingOther")
  abstract fun toCreatePreviousTrainingDto(
    request: CreateEducationAndQualificationsRequest?,
    prisonId: String,
  ): CreatePreviousTrainingDto?

  fun toEducationAndQualificationResponse(
    qualifications: PreviousQualifications?,
    training: PreviousTraining,
  ): EducationAndQualificationResponse {
    // We always expect to have a value within training (even if it only contains TrainingType.NONE), so we use that
    // as the "primary" object from which to populate the JPA generated fields from. However, if qualifications have
    // been updated more recently, then we populate modifiedDateTime and modifiedBy from there instead.
    var mostRecentModifiedDate = training.lastUpdatedAt!!
    var mostRecentModifiedBy = training.lastUpdatedBy!!
    if (qualifications != null && qualifications.lastUpdatedAt!!.isAfter(training.lastUpdatedAt)) {
      mostRecentModifiedDate = qualifications.lastUpdatedAt!!
      mostRecentModifiedBy = qualifications.lastUpdatedBy!!
    }
    return EducationAndQualificationResponse(
      id = training.reference,
      educationLevel = toHighestEducationLevelApi(qualifications?.educationLevel),
      qualifications = toAchievedQualifications(qualifications?.qualifications),
      additionalTraining = toTrainingTypesApi(training.trainingTypes),
      additionalTrainingOther = if (training.trainingTypes.contains(OTHER)) training.trainingTypeOther else null,
      modifiedBy = mostRecentModifiedBy,
      modifiedDateTime = toOffsetDateTime(mostRecentModifiedDate)!!,
    )
  }

  abstract fun toHighestEducationLevelDomain(educationLevel: HighestEducationLevelApi?): HighestEducationLevelDomain?

  abstract fun toHighestEducationLevelApi(educationLevel: HighestEducationLevelDomain?): HighestEducationLevelApi?

  private fun toAchievedQualifications(qualifications: List<Qualification>?): Set<AchievedQualification>? =
    qualifications?.map { toAchievedQualification(it) }?.toSet()

  private fun toAchievedQualification(qualification: Qualification): AchievedQualification =
    with(qualification) {
      AchievedQualification(
        subject = subject,
        level = AchievedQualification.Level.valueOf(level.name),
        grade = grade,
      )
    }

  abstract fun toTrainingTypesApi(trainingTypesDomain: List<TrainingTypeDomain>): Set<TrainingTypeApi>

  fun toOffsetDateTime(instant: Instant?): OffsetDateTime? = instant?.atOffset(ZoneOffset.UTC)

  @Mapping(target = "reference", source = "request.id")
  @Mapping(target = "educationLevel", source = "request.educationLevel", defaultValue = "NOT_SURE")
  abstract fun toUpdatePreviousQualificationsDto(
    request: UpdateEducationAndQualificationsRequest,
    prisonId: String,
  ): UpdatePreviousQualificationsDto?

  @Mapping(target = "reference", source = "request.id")
  @Mapping(target = "trainingTypes", source = "request.additionalTraining")
  @Mapping(target = "trainingTypeOther", source = "request.additionalTrainingOther")
  abstract fun toUpdatePreviousTrainingDto(
    request: UpdateEducationAndQualificationsRequest?,
    prisonId: String,
  ): UpdatePreviousTrainingDto?
}
