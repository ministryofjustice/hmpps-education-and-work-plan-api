package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.QualificationLevel
import java.util.UUID

sealed class UpdateOrCreateQualificationDto(
  open val subject: String,
  open val level: QualificationLevel,
  open val grade: String,
) {

  /**
   * DTO to create a new Qualification
   */
  data class CreateQualificationDto(
    override val subject: String,
    override val level: QualificationLevel,
    override val grade: String,
  ) : UpdateOrCreateQualificationDto(subject, level, grade)

  /**
   * DTO to update an existing Qualification identified by it's reference
   */
  data class UpdateQualificationDto(
    val reference: UUID,
    override val subject: String,
    override val level: QualificationLevel,
    override val grade: String,
  ) : UpdateOrCreateQualificationDto(subject, level, grade)
}
