package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import java.time.LocalDateTime

fun aValidEducationAndQualificationsResponse(
  educationLevel: HighestEducationLevel? = HighestEducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
  qualifications: Set<AchievedQualification>? = setOf(
    aValidAchievedQualification(),
  ),
  additionalTraining: Set<TrainingType> = setOf(TrainingType.OTHER),
  additionalTrainingOther: String? = "Kotlin course",
  modifiedBy: String = "bjones_gen",
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
