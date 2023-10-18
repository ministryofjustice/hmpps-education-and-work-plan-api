package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import java.util.UUID

fun aValidPreviousWorkExperiencesEntity(
  reference: UUID = UUID.randomUUID(),
  experiences: List<WorkExperienceEntity> = listOf(aValidWorkExperienceEntity()),
  createdAtPrison: String = "BXI",
  updatedAtPrison: String = "BXI",
) =
  PreviousWorkExperiencesEntity(
    reference = reference,
    experiences = experiences,
    createdAtPrison = createdAtPrison,
    updatedAtPrison = updatedAtPrison,
  )

fun aValidWorkExperienceEntity(
  reference: UUID = UUID.randomUUID(),
  experienceType: WorkExperienceType = WorkExperienceType.OTHER,
  experienceTypeOther: String = "Warehouse work",
  role: String? = "Chief Forklift Truck Driver",
  details: String? = "Forward, pick stuff up, reverse etc",
) =
  WorkExperienceEntity(
    reference = reference,
    experienceType = experienceType,
    experienceTypeOther = experienceTypeOther,
    role = role,
    details = details,
  )
