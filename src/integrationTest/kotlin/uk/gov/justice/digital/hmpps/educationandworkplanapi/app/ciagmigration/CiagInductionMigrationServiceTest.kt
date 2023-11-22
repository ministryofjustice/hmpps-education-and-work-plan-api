package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.anotherValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.repository.InductionMigrationRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.aValidCiagInductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.TimelineEventEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.TimelineEventType.INDUCTION_CREATED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.aValidTimelineEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.aValidTimelineEventEntity

class CiagInductionMigrationServiceTest : IntegrationTestBase() {

  @Autowired
  private lateinit var inductionMigrationService: CiagInductionMigrationService

  @Autowired
  private lateinit var inductionMigrationRepository: InductionMigrationRepository

  @Autowired
  private lateinit var wiremockService: WiremockService

  @BeforeEach
  fun resetWiremock() {
    wiremockService.resetAllStubsAndMappings()
  }

  @Test
  @Transactional
  fun `should import CIAG Inductions`() {
    // Given
    val prisonNumber1 = aValidPrisonNumber()
    val inductionCreatedEvent1 = aValidInductionCreatedTimelineEntity(prisonNumber = prisonNumber1)
    val prisonNumber2 = anotherValidPrisonNumber()
    val inductionCreatedEvent2 = aValidInductionCreatedTimelineEntity(prisonNumber = prisonNumber2)
    timelineRepository.saveAll(listOf(inductionCreatedEvent1, inductionCreatedEvent2))
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val inductionResponse1 = aValidCiagInductionResponse(offenderId = prisonNumber1)
    val inductionResponse2 = aValidCiagInductionResponse(offenderId = prisonNumber2)
    wiremockService.stubGetInductionFromCiagApi(inductionResponse1)
    wiremockService.stubGetInductionFromCiagApi(inductionResponse2)

    // When
    inductionMigrationService.migrateCiagInductions()

    // Then
    val importedInduction1 = inductionMigrationRepository.findByPrisonNumber(prisonNumber1)
    assertThat(importedInduction1).isNotNull
    assertThat(importedInduction1!!.prisonNumber).isEqualTo(prisonNumber1)

    val importedInduction2 = inductionMigrationRepository.findByPrisonNumber(prisonNumber2)
    assertThat(importedInduction2).isNotNull
    assertThat(importedInduction2!!.prisonNumber).isEqualTo(prisonNumber2)
  }

  fun aValidInductionCreatedTimelineEntity(
    prisonNumber: String = aValidPrisonNumber(),
    events: MutableList<TimelineEventEntity> = mutableListOf(aValidTimelineEventEntity(eventType = INDUCTION_CREATED)),
  ) = aValidTimelineEntity(
    prisonNumber = prisonNumber,
    events = events,
  )
}
