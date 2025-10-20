package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.QualificationLevel
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.UpdateOrCreateQualificationDto.CreateQualificationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.UpdateOrCreateQualificationDto.UpdateQualificationDto
import java.util.UUID

fun aValidCreateQualificationDto(
  prisonId: String = "BXI",
  subject: String = "English",
  level: QualificationLevel = QualificationLevel.LEVEL_1,
  grade: String = "C",
): CreateQualificationDto = CreateQualificationDto(
  prisonId = prisonId,
  subject = subject,
  level = level,
  grade = grade,
)

fun aValidUpdateQualificationDto(
  reference: UUID = UUID.randomUUID(),
  prisonId: String = "BXI",
  subject: String = "English",
  level: QualificationLevel = QualificationLevel.LEVEL_1,
  grade: String = "C",
): UpdateQualificationDto = UpdateQualificationDto(
  prisonId = prisonId,
  reference = reference,
  subject = subject,
  level = level,
  grade = grade,
)
