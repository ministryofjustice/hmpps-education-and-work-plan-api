package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.EmployabilitySkill
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.employabilityskill.dto.CreateEmployabilitySkillsDto

class EmployabilitySkillsService(
  private val persistenceAdapter: EmployabilitySkillPersistenceAdapter,
  private val employabilitySkillsEventService: EmployabilitySkillsEventService,
) {

  fun createEmployabilitySkills(createEmployabilitySkillsDto: CreateEmployabilitySkillsDto): List<EmployabilitySkill> = persistenceAdapter.createEmployabilitySkills(createEmployabilitySkillsDto)
    .also { employabilitySkillsEventService.employabilitySkillsCreated(it) }

  fun getEmployabilitySkills(prisonNumber: String): List<EmployabilitySkill> = persistenceAdapter.getEmployabilitySkills(prisonNumber)
}
