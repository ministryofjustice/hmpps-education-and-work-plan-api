package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import java.util.UUID

fun aValidInPrisonInterestsEntity(
  reference: UUID = UUID.randomUUID(),
  inPrisonWorkInterests: List<InPrisonWorkInterestEntity> = listOf(aValidInPrisonWorkInterestEntity()),
  inPrisonTrainingInterests: List<InPrisonTrainingInterestEntity> = listOf(aValidInPrisonTrainingInterestEntity()),
  createdAtPrison: String = "BXI",
  updatedAtPrison: String = "BXI",
) =
  InPrisonInterestsEntity(
    reference = reference,
    inPrisonWorkInterests = inPrisonWorkInterests,
    inPrisonTrainingInterests = inPrisonTrainingInterests,
    createdAtPrison = createdAtPrison,
    updatedAtPrison = updatedAtPrison,
  )

fun aValidInPrisonWorkInterestEntity(
  reference: UUID = UUID.randomUUID(),
  workType: InPrisonWorkType = InPrisonWorkType.OTHER,
  workTypeOther: String? = "Any work type",
) =
  InPrisonWorkInterestEntity(
    reference = reference,
    workType = workType,
    workTypeOther = workTypeOther,
  )

fun aValidInPrisonTrainingInterestEntity(
  reference: UUID = UUID.randomUUID(),
  trainingType: InPrisonTrainingType = InPrisonTrainingType.OTHER,
  trainingTypeOther: String? = "Any training type",
) =
  InPrisonTrainingInterestEntity(
    reference = reference,
    trainingType = trainingType,
    trainingTypeOther = trainingTypeOther,
  )
