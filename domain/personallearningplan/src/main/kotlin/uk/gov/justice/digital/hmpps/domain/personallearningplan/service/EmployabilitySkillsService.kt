package uk.gov.justice.digital.hmpps.domain.personallearningplan.service

import uk.gov.justice.digital.hmpps.domain.personallearningplan.EmployabilitySkill
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateEmployabilitySkillsDto

class EmployabilitySkillsService(
  private val persistenceAdapter: EmployabilitySkillPersistenceAdapter,
  private val employabilitySkillsEventService: EmployabilitySkillsEventService,
) {

  fun createEmployabilitySkills(createEmployabilitySkillsDto: CreateEmployabilitySkillsDto): List<EmployabilitySkill> = persistenceAdapter.createEmployabilitySkills(createEmployabilitySkillsDto)
    .also { employabilitySkillsEventService.employabilitySkillsCreated(it) }
}
