package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionSchedulePersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction.InductionScheduleEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionScheduleRepository

@Component
class JpaInductionSchedulePersistenceAdapter(
  private val inductionScheduleRepository: InductionScheduleRepository,
  private val inductionScheduleEntityMapper: InductionScheduleEntityMapper,
) : InductionSchedulePersistenceAdapter {
  @Transactional
  override fun createInductionSchedule(createInductionScheduleDto: CreateInductionScheduleDto): InductionSchedule =
    inductionScheduleRepository.saveAndFlush(inductionScheduleEntityMapper.fromCreateDtoToEntity(createInductionScheduleDto)).let {
      inductionScheduleEntityMapper.fromEntityToDomain(it)
    }

  @Transactional(readOnly = true)
  override fun getInductionSchedule(prisonNumber: String): InductionSchedule? =
    inductionScheduleRepository.findByPrisonNumber(prisonNumber)?.let {
      inductionScheduleEntityMapper.fromEntityToDomain(it)
    }
}
