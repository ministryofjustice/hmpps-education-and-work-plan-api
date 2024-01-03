package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateEducationAndQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HighestEducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateEducationAndQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidAchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.anotherValidAchievedQualification
import java.util.UUID

fun aValidCreateEducationAndQualificationsRequest(
  educationLevel: HighestEducationLevel? = HighestEducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
  qualifications: Set<AchievedQualification>? = setOf(
    aValidAchievedQualification(),
    anotherValidAchievedQualification(),
  ),
  additionalTraining: Set<TrainingType> = setOf(TrainingType.OTHER),
  additionalTrainingOther: String? = "Any training",
): CreateEducationAndQualificationsRequest = CreateEducationAndQualificationsRequest(
  educationLevel = educationLevel,
  qualifications = qualifications,
  additionalTraining = additionalTraining,
  additionalTrainingOther = additionalTrainingOther,
)

fun aValidUpdateEducationAndQualificationsRequest(
  id: UUID? = UUID.randomUUID(),
  educationLevel: HighestEducationLevel? = HighestEducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
  qualifications: Set<AchievedQualification>? = setOf(
    aValidAchievedQualification(),
    anotherValidAchievedQualification(),
  ),
  additionalTraining: Set<TrainingType>? = setOf(TrainingType.CSCS_CARD, TrainingType.OTHER),
  additionalTrainingOther: String? = "Any training",
): UpdateEducationAndQualificationsRequest = UpdateEducationAndQualificationsRequest(
  id = id,
  educationLevel = educationLevel,
  qualifications = qualifications,
  additionalTraining = additionalTraining,
  additionalTrainingOther = additionalTrainingOther,
)
