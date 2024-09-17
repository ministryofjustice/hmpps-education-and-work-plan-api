package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.PreviousQualifications
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.CreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.dto.UpdatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.service.EducationPersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction.PreviousQualificationsEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.PreviousQualificationsRepository

private val log = KotlinLogging.logger {}

@Component
class JpaEducationPersistenceAdapter(
  private val previousQualificationsRepository: PreviousQualificationsRepository,
  private val previousQualificationsMapper: PreviousQualificationsEntityMapper,
) : EducationPersistenceAdapter {
  @Transactional(readOnly = true)
  override fun getPreviousQualifications(prisonNumber: String): PreviousQualifications? =
    previousQualificationsRepository.findByPrisonNumber(prisonNumber)?.let {
      previousQualificationsMapper.fromEntityToDomain(it)
    }

  @Transactional
  override fun createPreviousQualifications(createPreviousQualificationsDto: CreatePreviousQualificationsDto): PreviousQualifications {
    val previousQualificationsEntity = previousQualificationsRepository.saveAndFlush(
      previousQualificationsMapper.fromCreateDtoToEntity(createPreviousQualificationsDto),
    )
    return previousQualificationsMapper.fromEntityToDomain(previousQualificationsEntity)!!
  }

  @Transactional
  override fun updatePreviousQualifications(updatePreviousQualificationsDto: UpdatePreviousQualificationsDto): PreviousQualifications? {
    val previousQualificationsEntity = previousQualificationsRepository.findByPrisonNumber(updatePreviousQualificationsDto.prisonNumber)?.takeIf {
      it.reference == updatePreviousQualificationsDto.reference
    }

    return if (previousQualificationsEntity != null) {
      previousQualificationsMapper.updateExistingEntityFromDto(previousQualificationsEntity, updatePreviousQualificationsDto)
      val persistedEntity = previousQualificationsRepository.saveAndFlush(previousQualificationsEntity)
      return previousQualificationsMapper.fromEntityToDomain(persistedEntity)
    } else {
      null
    }
  }
}
