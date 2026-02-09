package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.domain.personallearningplan.EmployabilitySkill
import uk.gov.justice.digital.hmpps.domain.personallearningplan.EmployabilitySkillNotFoundException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateEmployabilitySkillsDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.EmployabilitySkillPersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.employabilityskill.EmployabilitySkillEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.EmployabilitySkillRepository
import java.util.UUID

@Component
class JpaEmployabilitySkillPersistenceAdapter(
  private val employabilitySkillRepository: EmployabilitySkillRepository,
  private val employabilitySkillMapper: EmployabilitySkillEntityMapper,
) : EmployabilitySkillPersistenceAdapter {

  @Transactional
  override fun createEmployabilitySkills(createEmployabilitySkillsDto: CreateEmployabilitySkillsDto): List<EmployabilitySkill> {
    val employabilitySkills =
      employabilitySkillRepository.saveAllAndFlush(employabilitySkillMapper.fromDtoToEntity(createEmployabilitySkillsDto))
    return employabilitySkills.map { employabilitySkillMapper.fromEntityToDomain(it) }
  }

  @Transactional(readOnly = true)
  override fun getEmployabilitySkills(prisonNumber: String): List<EmployabilitySkill> = employabilitySkillRepository.findByPrisonNumber(prisonNumber).map {
    employabilitySkillMapper.fromEntityToDomain(it)
  }

  @Transactional(readOnly = true)
  override fun getEmployabilitySkill(prisonNumber: String, reference: UUID): EmployabilitySkill = employabilitySkillRepository
    .findByPrisonNumberAndReference(prisonNumber, reference)
    ?.let(employabilitySkillMapper::fromEntityToDomain)
    ?: throw EmployabilitySkillNotFoundException(prisonNumber, reference)
}
