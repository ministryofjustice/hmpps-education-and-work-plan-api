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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.HighestEducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.HopingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.InPrisonTrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.InPrisonWorkType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.InterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.SkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.TrainingType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.WorkExperienceType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.WorkInterestType
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
    assertThat(importedInduction1.workOnRelease!!.hopingToWork).isEqualTo(HopingToWork.NOT_SURE)
    assertThat(importedInduction1.previousQualifications!!.educationLevel).isEqualTo(SECONDARY_SCHOOL_TOOK_EXAMS)
    assertThat(importedInduction1.previousTraining!!.trainingTypes).containsExactly(TrainingType.OTHER)
    assertThat(importedInduction1.previousWorkExperiences!!.experiences!![0].experienceType).isEqualTo(WorkExperienceType.OTHER)
    assertThat(importedInduction1.inPrisonInterests!!.inPrisonWorkInterests!![0].workType).isEqualTo(InPrisonWorkType.OTHER)
    assertThat(importedInduction1.inPrisonInterests!!.inPrisonTrainingInterests!![0].trainingType).isEqualTo(InPrisonTrainingType.OTHER)
    assertThat(importedInduction1.personalSkillsAndInterests!!.skills!![0].skillType).isEqualTo(SkillType.OTHER)
    assertThat(importedInduction1.personalSkillsAndInterests!!.interests!![0].interestType).isEqualTo(InterestType.OTHER)
    assertThat(importedInduction1.futureWorkInterests!!.interests!![0].workType).isEqualTo(WorkInterestType.OTHER)

    val importedInduction2 = inductionMigrationRepository.findByPrisonNumber(prisonNumber2)
    assertThat(importedInduction2).isNotNull
    assertThat(importedInduction2!!.prisonNumber).isEqualTo(prisonNumber2)
    assertThat(importedInduction2.workOnRelease!!.hopingToWork).isEqualTo(HopingToWork.NOT_SURE)
    assertThat(importedInduction2.previousQualifications!!.educationLevel).isEqualTo(SECONDARY_SCHOOL_TOOK_EXAMS)
    assertThat(importedInduction2.previousTraining!!.trainingTypes).containsExactly(TrainingType.OTHER)
    assertThat(importedInduction2.previousWorkExperiences!!.experiences!![0].experienceType).isEqualTo(WorkExperienceType.OTHER)
    assertThat(importedInduction2.inPrisonInterests!!.inPrisonWorkInterests!![0].workType).isEqualTo(InPrisonWorkType.OTHER)
    assertThat(importedInduction2.inPrisonInterests!!.inPrisonTrainingInterests!![0].trainingType).isEqualTo(InPrisonTrainingType.OTHER)
    assertThat(importedInduction2.personalSkillsAndInterests!!.skills!![0].skillType).isEqualTo(SkillType.OTHER)
    assertThat(importedInduction2.personalSkillsAndInterests!!.interests!![0].interestType).isEqualTo(InterestType.OTHER)
    assertThat(importedInduction2.futureWorkInterests!!.interests!![0].workType).isEqualTo(WorkInterestType.OTHER)
  }

  fun aValidInductionCreatedTimelineEntity(
    prisonNumber: String = aValidPrisonNumber(),
    events: MutableList<TimelineEventEntity> = mutableListOf(aValidTimelineEventEntity(eventType = INDUCTION_CREATED)),
  ) = aValidTimelineEntity(
    prisonNumber = prisonNumber,
    events = events,
  )
}
