package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration

import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.repository.InductionMigrationRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.mapper.InductionMigrationMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.CiagInductionResponse

private val log = KotlinLogging.logger {}

@Component
class CiagInductionPersistenceService(
  private val inductionMigrationRepository: InductionMigrationRepository,
  private val inductionMigrationMapper: InductionMigrationMapper,
) {

  @Transactional
  fun saveInduction(ciagInduction: CiagInductionResponse) {
    val prisonNumber = ciagInduction.offenderId
    if (inductionMigrationRepository.findByPrisonNumber(prisonNumber) != null) {
      log.warn { "Induction for Prisoner $prisonNumber has already been migrated" }
      return
    }

    log.info { "Migrating Induction for prisoner $prisonNumber" }
    inductionMigrationRepository.save(inductionMigrationMapper.toInductionMigrationEntity(ciagInduction))
  }
}
