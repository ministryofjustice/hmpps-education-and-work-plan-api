package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreateInductionScheduleDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.UserDetailsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aPersistedInductionScheduleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.anUnPersistedInductionScheduleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ManageUserService
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule as InductionScheduleCalculationRuleDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus as InductionScheduleStatusDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleCalculationRule as InductionScheduleCalculationRuleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus as InductionScheduleStatusEntity

@ExtendWith(MockitoExtension::class)
class InductionScheduleEntityMapperTest {
  @InjectMocks
  private lateinit var mapper: InductionScheduleEntityMapper

  @Mock
  private lateinit var instantMapper: InstantMapper

  @Mock
  private lateinit var userService: ManageUserService

  @Test
  fun `should map entity to domain`() {
    // Given

    given(userService.getUserDetails(any())).willReturn(
      UserDetailsDto("asmith_gen", true, "Alex Smith"),
      UserDetailsDto("bjones_gen", true, "Barry Jones"),
    )

    val id = UUID.randomUUID()
    val reference = UUID.randomUUID()
    val deadlineDate = LocalDate.now().plusMonths(3)
    val prisonNumber = aValidPrisonNumber()

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
    val prisonNumber = aValidPrisonNumber()

    val createInductionScheduleDto = aValidCreateInductionScheduleDto(
      prisonNumber = prisonNumber,
      deadlineDate = deadlineDate,
      scheduleCalculationRule = InductionScheduleCalculationRuleDomain.EXISTING_PRISONER_ON_REMAND,
    )

    val expectedInductionScheduleEntity = anUnPersistedInductionScheduleEntity(
      prisonNumber = prisonNumber,
      deadlineDate = deadlineDate,
      scheduleCalculationRule = InductionScheduleCalculationRuleEntity.EXISTING_PRISONER_ON_REMAND,
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
