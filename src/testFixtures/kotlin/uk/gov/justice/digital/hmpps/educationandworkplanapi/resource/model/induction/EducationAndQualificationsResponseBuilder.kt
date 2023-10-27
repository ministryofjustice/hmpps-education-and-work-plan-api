package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationAndQualificationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HighestEducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TrainingType
import java.time.OffsetDateTime
import java.util.UUID

fun aValidEducationAndQualificationsResponse(
  id: UUID? = UUID.randomUUID(),
  educationLevel: HighestEducationLevel? = HighestEducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
  qualifications: Set<AchievedQualification>? = setOf(
    aValidAchievedQualification(),
    anotherValidAchievedQualification(),
  ),
  additionalTraining: Set<TrainingType>? = setOf(TrainingType.CSCS_CARD, TrainingType.OTHER),
  additionalTrainingOther: String? = "Any training",
  modifiedBy: String = "asmith_gen",
  modifiedDateTime: OffsetDateTime = OffsetDateTime.now(),
): EducationAndQualificationResponse =
  EducationAndQualificationResponse(
    id = id,
    educationLevel = educationLevel,
    qualifications = qualifications,
    additionalTraining = additionalTraining,
    additionalTrainingOther = additionalTrainingOther,
    modifiedBy = modifiedBy,
    modifiedDateTime = modifiedDateTime,
  )
