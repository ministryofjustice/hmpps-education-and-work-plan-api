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

  @Mapping(target = "qualifications", source = "request.qualifications")
  abstract fun toCreatePreviousQualificationsDto(
    request: CreateEducationAndQualificationsRequest?,
    prisonId: String,
  ): CreatePreviousQualificationsDto?

  abstract fun toQualification(qualificationRequest: AchievedQualification): Qualification

  @Mapping(target = "trainingTypes", source = "request.additionalTraining")
  @Mapping(target = "trainingTypeOther", source = "request.additionalTrainingOther")
  abstract fun toCreatePreviousTrainingDto(
    request: CreateEducationAndQualificationsRequest?,
    prisonId: String,
  ): CreatePreviousTrainingDto?

  fun toEducationAndQualificationResponse(
    qualifications: PreviousQualifications?,
    training: PreviousTraining?,
  ): EducationAndQualificationResponse? {
    return training?.let {
      EducationAndQualificationResponse(
        // We always expect to have a value within PreviousTraining (even if it only contains just TrainingType.NONE),
        // so we use PreviousTraining as the parent/primary object. However, we are still guarding against it being
        // null in case anything changes on the client side.
        id = training.reference,
        educationLevel = toHighestEducationLevelApi(qualifications?.educationLevel),
        qualifications = toAchievedQualifications(qualifications?.qualifications),
        additionalTraining = toTrainingTypesApi(training.trainingTypes),
        additionalTrainingOther = if (training.trainingTypes.contains(OTHER)) training.trainingTypeOther else null,
        modifiedBy = training.lastUpdatedBy!!,
        modifiedDateTime = toOffsetDateTime(training.lastUpdatedAt)!!,
      )
    }
  }

  abstract fun toHighestEducationLevelApi(educationLevel: HighestEducationLevelDomain?): HighestEducationLevelApi?

  private fun toAchievedQualifications(qualifications: List<Qualification>?): Set<AchievedQualification>? =
    qualifications?.map { toAchievedQualification(it) }?.toSet()?.ifEmpty { null }

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
  @Mapping(target = "qualifications", source = "request.qualifications")
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
