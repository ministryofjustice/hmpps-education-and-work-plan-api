package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.Induction
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSummary
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreatePreviousQualificationsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateInductionDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionPersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousQualificationsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction.InductionEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction.PreviousQualificationsEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.PreviousQualificationsRepository

private val log = KotlinLogging.logger {}

@Component
class JpaInductionPersistenceAdapter(
  private val inductionRepository: InductionRepository,
  private val inductionMapper: InductionEntityMapper,
  private val previousQualificationsRepository: PreviousQualificationsRepository,
  private val previousQualificationsMapper: PreviousQualificationsEntityMapper,
) : InductionPersistenceAdapter {

  @Transactional
  override fun createInduction(createInductionDto: CreateInductionDto): Induction {
    val prisonNumber = createInductionDto.prisonNumber
    val inductionEntity = inductionRepository.saveAndFlush(inductionMapper.fromCreateDtoToEntity(createInductionDto))
    val previousQualificationsEntity = createOrUpdatePreviousQualifications(createInductionDto, prisonNumber)
    return inductionMapper.fromEntityToDomain(inductionEntity, previousQualificationsEntity)
  }

  @Transactional(readOnly = true)
  override fun getInduction(prisonNumber: String): Induction? =
    inductionRepository.findByPrisonNumber(prisonNumber)?.let {
      val previousQualificationsEntity = previousQualificationsRepository.findByPrisonNumber(prisonNumber)
      inductionMapper.fromEntityToDomain(it, previousQualificationsEntity)
    }

  @Transactional
  override fun updateInduction(updateInductionDto: UpdateInductionDto): Induction? {
    val prisonNumber = updateInductionDto.prisonNumber
    val inductionEntity = inductionRepository.findByPrisonNumber(prisonNumber)
    return if (inductionEntity != null) {
      inductionMapper.updateEntityFromDto(inductionEntity, updateInductionDto)
      inductionEntity.updateLastUpdatedAt() // force the main Induction's JPA managed fields to update
      val updatedInductionEntity = inductionRepository.saveAndFlush(inductionEntity)
      val previousQualificationsEntity = createOrUpdatePreviousQualifications(updateInductionDto, prisonNumber)
      inductionMapper.fromEntityToDomain(updatedInductionEntity, previousQualificationsEntity)
    } else {
      null
    }
  }

  @Transactional(readOnly = true)
  override fun getInductionSummaries(prisonNumbers: List<String>): List<InductionSummary> =
    inductionRepository.findByPrisonNumberIn(prisonNumbers).let {
      inductionMapper.fromEntitySummariesToDomainSummaries(it)
    }

  private fun createOrUpdatePreviousQualifications(
    updateInductionDto: UpdateInductionDto,
    prisonNumber: String,
  ): PreviousQualificationsEntity? {
    val previousQualificationsEntity = previousQualificationsRepository.findByPrisonNumber(prisonNumber)

    if (updateInductionDto.previousQualifications == null) {
      // If previousQualifications on the DTO is null it means no change is required to the prisoner's previousQualifications. Return the entity.
      return previousQualificationsEntity
    }

    // The DTO contains previousQualifications. Behaviour now depends on whether the prisoner already has previousQualifications or not.
    if (previousQualificationsEntity != null) {
      // Prisoner already has previousQualifications. We need to update them.
      if (updateInductionDto.previousQualifications!!.qualifications.isEmpty()) {
        log.info {
          """
            Prisoner [$prisonNumber] has [${updateInductionDto.previousQualifications!!.qualifications.size}] qualifications recorded, but the Update Induction request contains a PreviousQualifications object containing 0 qualifications. 
            The user has explicitly removed all previously recorded qualifications as part of updating the prisoner's Induction.
          """.trimIndent()
        }
      }
      previousQualificationsMapper.updateExistingEntityFromDto(
        previousQualificationsEntity,
        updateInductionDto.previousQualifications,
      )
      return previousQualificationsRepository.saveAndFlush(previousQualificationsEntity)
    } else {
      // Prisoner does not already have previous qualifications, and the DTO has qualifications. We need to create them
      val createPreviousQualificationsDto = with(updateInductionDto.previousQualifications!!) {
        CreatePreviousQualificationsDto(
          prisonNumber = prisonNumber,
          educationLevel = educationLevel!!,
          qualifications = qualifications,
          prisonId = prisonId,
        )
      }
      return previousQualificationsRepository.saveAndFlush(
        previousQualificationsMapper.fromCreateDtoToEntity(createPreviousQualificationsDto)!!,
      )
    }
  }

  private fun createOrUpdatePreviousQualifications(
    createInductionDto: CreateInductionDto,
    prisonNumber: String,
  ): PreviousQualificationsEntity? {
    val previousQualificationsEntity = previousQualificationsRepository.findByPrisonNumber(prisonNumber)

    if (createInductionDto.previousQualifications == null) {
      // If previousQualifications on the DTO is null it means the CIAG is creating an Induction with the specific intent of the prisoner having no previous qualifications.
      // In this case we should delete the previousQualifications entity if it exists (because regardless of any previously added qualifications for the prisoner, the CIAG is
      // saying that the Induction should be created with no qualifications)
      previousQualificationsEntity?.also {
        log.info {
          """
            Prisoner [$prisonNumber] has [${it.qualifications!!.size}] qualifications recorded pre-induction, but the Create Induction request does not contain a PreviousQualifications object. 
            Removing the prisoner's PreviousQualificationsEntity from the database.
          """.trimIndent()
        }
        previousQualificationsRepository.delete(it)
      }
      return null
    }

    // The DTO contains previousQualifications. Behaviour now depends on whether the prisoner already has previousQualifications or not.
    if (previousQualificationsEntity != null) {
      // Prisoner already has previousQualifications. We need to update them.
      if (createInductionDto.previousQualifications!!.qualifications.isEmpty()) {
        log.info {
          """
            Prisoner [$prisonNumber] has [${createInductionDto.previousQualifications!!.qualifications.size}] qualifications recorded pre-induction, but the Create Induction request contains a PreviousQualifications object containing 0 qualifications. 
            The user has explicitly removed all previously recorded qualifications as part of creating the prisoner's Induction.
          """.trimIndent()
        }
      }
      previousQualificationsMapper.updateExistingEntityFromDto(
        previousQualificationsEntity,
        createInductionDto.previousQualifications,
      )
      return previousQualificationsRepository.saveAndFlush(previousQualificationsEntity)
    } else {
      // Prisoner does not already have previous qualifications, and the DTO has qualifications. We need to create them
      return previousQualificationsRepository.saveAndFlush(
        previousQualificationsMapper.fromCreateDtoToEntity(createInductionDto.previousQualifications)!!,
      )
    }
  }
}
