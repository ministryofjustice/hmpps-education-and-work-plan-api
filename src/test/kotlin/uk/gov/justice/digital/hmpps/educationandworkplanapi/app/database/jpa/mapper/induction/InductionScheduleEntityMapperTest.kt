package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreateInductionScheduleDto
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aPersistedInductionScheduleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.anUnPersistedInductionScheduleEntity
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule as InductionScheduleCalculationRuleDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus as InductionScheduleStatusDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleCalculationRule as InductionScheduleCalculationRuleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus as InductionScheduleStatusEntity

class InductionScheduleEntityMapperTest {
  private val mapper = InductionScheduleEntityMapper()

  @Test
  fun `should map entity to domain`() {
    // Given
    val id = UUID.randomUUID()
    val reference = UUID.randomUUID()
    val deadlineDate = LocalDate.now().plusMonths(3)
    val prisonNumber = randomValidPrisonNumber()

    val inductionScheduleEntity = aPersistedInductionScheduleEntity(
      id = id,
      reference = reference,
      prisonNumber = prisonNumber,
      deadlineDate = deadlineDate,
      scheduleCalculationRule = InductionScheduleCalculationRuleEntity.NEW_PRISON_ADMISSION,
      scheduleStatus = InductionScheduleStatusEntity.SCHEDULED,
      createdAt = Instant.now(),
      createdBy = "asmith_gen",
      updatedAt = Instant.now(),
      updatedBy = "bjones_gen",
    )

    val expected = aValidInductionSchedule(
      reference = reference,
      prisonNumber = prisonNumber,
      deadlineDate = deadlineDate,
      scheduleCalculationRule = InductionScheduleCalculationRuleDomain.NEW_PRISON_ADMISSION,
      scheduleStatus = InductionScheduleStatusDomain.SCHEDULED,
      createdBy = "asmith_gen",
      lastUpdatedBy = "bjones_gen",
    )

    // When
    val actual = mapper.fromEntityToDomain(inductionScheduleEntity)

    // Then
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFields("createdAt", "lastUpdatedAt")
      .isEqualTo(expected)
  }

  @Test
  fun `should map createDto to entity`() {
    // Given
    val deadlineDate = LocalDate.now().plusMonths(3)
    val prisonNumber = randomValidPrisonNumber()

    val createInductionScheduleDto = aValidCreateInductionScheduleDto(
      prisonNumber = prisonNumber,
      deadlineDate = deadlineDate,
      scheduleCalculationRule = InductionScheduleCalculationRuleDomain.EXISTING_PRISONER,
      scheduleStatus = InductionScheduleStatusDomain.SCHEDULED,
    )

    val expectedInductionScheduleEntity = anUnPersistedInductionScheduleEntity(
      prisonNumber = prisonNumber,
      deadlineDate = deadlineDate,
      scheduleCalculationRule = InductionScheduleCalculationRuleEntity.EXISTING_PRISONER,
      scheduleStatus = InductionScheduleStatusEntity.SCHEDULED,
    )

    // When
    val actual = mapper.fromCreateDtoToEntity(createInductionScheduleDto)

    // Then
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFields("reference")
      .isEqualTo(expectedInductionScheduleEntity)
  }
}
