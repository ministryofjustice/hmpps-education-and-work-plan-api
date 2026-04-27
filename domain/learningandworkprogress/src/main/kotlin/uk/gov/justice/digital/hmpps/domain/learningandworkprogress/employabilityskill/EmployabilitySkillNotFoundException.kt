package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill

import java.util.UUID

/**
 * Thrown when an Employability skill cannot be found.
 */
class EmployabilitySkillNotFoundException(prisonNumber: String, reference: UUID) : RuntimeException("EmployabilitySkill for prisoner [$prisonNumber] and reference [$reference] not found")
