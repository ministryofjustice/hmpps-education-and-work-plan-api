package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction.InductionEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.Induction
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateInductionDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.UpdateInductionDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.service.InductionPersistenceAdapter

@Component
class JpaInductionPersistenceAdapter(
  private val inductionRepository: InductionRepository,
  private val inductionMapper: InductionEntityMapper,
) : InductionPersistenceAdapter {

  @Transactional
  override fun createInduction(createInductionDto: CreateInductionDto): Induction {
    val persistedEntity = inductionRepository.save(inductionMapper.fromCreateDtoToEntity(createInductionDto))
    return inductionMapper.fromEntityToDomain(persistedEntity)
  }

  @Transactional(readOnly = true)
  override fun getInduction(prisonNumber: String): Induction? =
    inductionRepository.findByPrisonNumber(prisonNumber)?.let {
      inductionMapper.fromEntityToDomain(it)
    }

  @Transactional
  override fun updateInduction(updateInductionDto: UpdateInductionDto): Induction? {
    val inductionEntity = inductionRepository.findByPrisonNumber(updateInductionDto.prisonNumber)
    return if (inductionEntity != null) {
      inductionMapper.updateEntityFromDto(inductionEntity, updateInductionDto)
      inductionMapper.fromEntityToDomain(inductionEntity)
    } else {
      null
    }
  }
}
