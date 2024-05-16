package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HighestEducationLevel
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HighestEducationLevel.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.Qualification
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidQualification
import java.util.UUID

fun aValidCreatePreviousQualificationsDto(
  educationLevel: HighestEducationLevel = SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS,
  qualifications: List<Qualification> = listOf(aValidQualification()),
  prisonId: String = "BXI",
) =
  CreatePreviousQualificationsDto(
    educationLevel = educationLevel,
    qualifications = qualifications,
    prisonId = prisonId,
  )

fun aValidUpdatePreviousQualificationsDto(
  reference: UUID = UUID.randomUUID(),
  educationLevel: HighestEducationLevel = SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS,
  qualifications: List<Qualification> = listOf(aValidQualification()),
  prisonId: String = "BXI",
) =
  UpdatePreviousQualificationsDto(
    reference = reference,
    educationLevel = educationLevel,
    qualifications = qualifications,
    prisonId = prisonId,
  )
