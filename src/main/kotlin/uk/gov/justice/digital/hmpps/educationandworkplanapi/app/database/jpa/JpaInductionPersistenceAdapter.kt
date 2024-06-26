package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.Induction
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSummary
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateInductionDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionPersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction.InductionEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionRepository

@Component
class JpaInductionPersistenceAdapter(
  private val inductionRepository: InductionRepository,
  private val inductionMapper: InductionEntityMapper,
) : InductionPersistenceAdapter {

  @Transactional
  override fun createInduction(createInductionDto: CreateInductionDto): Induction {
    val persistedEntity = inductionRepository.saveAndFlush(inductionMapper.fromCreateDtoToEntity(createInductionDto))
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
      inductionEntity.updateLastUpdatedAt() // force the main Induction's JPA managed fields to update
      val persistedEntity = inductionRepository.saveAndFlush(inductionEntity)
      inductionMapper.fromEntityToDomain(persistedEntity)
    } else {
      null
    }
  }

  @Transactional(readOnly = true)
  override fun getInductionSummaries(prisonNumbers: List<String>): List<InductionSummary> =
    inductionRepository.findByPrisonNumberIn(prisonNumbers).let {
      inductionMapper.fromEntitySummariesToDomainSummaries(it)
    }
}
