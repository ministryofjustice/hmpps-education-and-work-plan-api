package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateEducationAndQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HighestEducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateEducationAndQualificationsRequest
import java.util.UUID

fun aValidCreateEducationAndQualificationsRequest(
  educationLevel: HighestEducationLevel? = HighestEducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
  qualifications: Set<AchievedQualification>? = setOf(
    aValidAchievedQualification(),
    anotherValidAchievedQualification(),
  ),
  additionalTraining: Set<TrainingType>? = setOf(TrainingType.OTHER),
  additionalTrainingOther: String? = "Any training",
): CreateEducationAndQualificationsRequest = CreateEducationAndQualificationsRequest(
  educationLevel = educationLevel,
  qualifications = qualifications,
  additionalTraining = additionalTraining,
  additionalTrainingOther = additionalTrainingOther,
)

fun aValidUpdateEducationAndQualificationsRequest(
  id: UUID = UUID.randomUUID(),
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

fun aValidAchievedQualification(
  subject: String? = "English",
  level: AchievedQualification.Level? = AchievedQualification.Level.LEVEL_3,
  grade: String? = "A",
): AchievedQualification = AchievedQualification(
  subject = subject,
  level = level,
  grade = grade,
)

fun anotherValidAchievedQualification(
  subject: String? = "Maths",
  level: AchievedQualification.Level? = AchievedQualification.Level.LEVEL_3,
  grade: String? = "B",
): AchievedQualification = AchievedQualification(
  subject = subject,
  level = level,
  grade = grade,
)
