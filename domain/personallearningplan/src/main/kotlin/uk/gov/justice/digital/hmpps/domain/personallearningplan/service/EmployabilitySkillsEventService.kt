package uk.gov.justice.digital.hmpps.domain.personallearningplan.service

import uk.gov.justice.digital.hmpps.domain.personallearningplan.EmployabilitySkill

/**
 * Interface for defining [EmployabilitySkill] related lifecycle events.
 */
interface EmployabilitySkillsEventService {

  /**
   * Implementations providing custom code for when an [EmployabilitySkill] is created.
   */
  fun employabilitySkillsCreated(employabilitySkills: List<EmployabilitySkill>)
}
