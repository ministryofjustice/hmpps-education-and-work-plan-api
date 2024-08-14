package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationLevel
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationLevel.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.Qualification
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.aValidQualification
import java.util.UUID

fun aValidCreatePreviousQualificationsDto(
  prisonNumber: String = "A1234AB",
  educationLevel: EducationLevel = SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS,
  qualifications: List<Qualification> = listOf(aValidQualification()),
  prisonId: String = "BXI",
) =
  CreatePreviousQualificationsDto(
    prisonNumber = prisonNumber,
    educationLevel = educationLevel,
    qualifications = qualifications,
    prisonId = prisonId,
  )

fun aValidUpdatePreviousQualificationsDto(
  reference: UUID = UUID.randomUUID(),
  educationLevel: EducationLevel = SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS,
  qualifications: List<Qualification> = listOf(aValidQualification()),
  prisonId: String = "BXI",
) =
  UpdatePreviousQualificationsDto(
    reference = reference,
    educationLevel = educationLevel,
    qualifications = qualifications,
    prisonId = prisonId,
  )
