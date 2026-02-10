package uk.gov.justice.digital.hmpps.domain.personallearningplan.service

import uk.gov.justice.digital.hmpps.domain.personallearningplan.EmployabilitySkill
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateEmployabilitySkillsDto
import java.util.UUID

/**
 * Persistence Adapter for [EmployabilitySkill] instances.
 *
 * Implementations should use the underlying persistence of the application in question, eg: JPA, Mongo, Dynamo, Redis etc
 *
 * Implementations should not throw exceptions. These are not part of the interface and are not checked or handled by
 * [EmployabilitySkillsService].
 */
interface EmployabilitySkillPersistenceAdapter {

  /**
   * Creates a new [EmployabilitySkill] and returns persisted instance.
   */
  fun createEmployabilitySkills(createEmployabilitySkillsDto: CreateEmployabilitySkillsDto): List<EmployabilitySkill>

  /**
   * Returns an [EmployabilitySkill] if found, otherwise `null`.
   */
  fun getEmployabilitySkill(prisonNumber: String, reference: UUID): EmployabilitySkill?

  /**
   * Returns a list of [EmployabilitySkill] if found, otherwise empty list.
   */
  fun getEmployabilitySkills(prisonNumber: String): List<EmployabilitySkill>
}
