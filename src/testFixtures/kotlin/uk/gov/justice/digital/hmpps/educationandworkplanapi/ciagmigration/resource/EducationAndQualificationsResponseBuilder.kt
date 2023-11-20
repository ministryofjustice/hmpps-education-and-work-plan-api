package uk.gov.justice.digital.hmpps.educationandworkplanapi.ciagmigration.resource

import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.AchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.EducationAndQualificationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.HighestEducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.TrainingType
import java.time.LocalDateTime
import java.util.UUID

fun aValidEducationAndQualificationsResponse(
  id: UUID? = UUID.randomUUID(),
  educationLevel: HighestEducationLevel? = HighestEducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
  qualifications: Set<AchievedQualification>? = setOf(
    aValidAchievedQualification(),
    anotherValidAchievedQualification(),
  ),
  additionalTraining: Set<TrainingType> = setOf(TrainingType.OTHER),
  additionalTrainingOther: String? = "Any training",
  modifiedBy: String = "auser_gen",
  modifiedDateTime: LocalDateTime = LocalDateTime.now(),
): EducationAndQualificationResponse =
  EducationAndQualificationResponse(
    educationLevel = educationLevel,
    qualifications = qualifications,
    additionalTraining = additionalTraining,
    additionalTrainingOther = additionalTrainingOther,
    modifiedBy = modifiedBy,
    modifiedDateTime = modifiedDateTime,
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
