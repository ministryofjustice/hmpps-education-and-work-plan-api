package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.specialtests

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.INDUCTIONS_RW
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdateInductionScheduleStatusRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus as InductionScheduleStatusEntity

@Isolated
@ActiveProfiles("integration-test", "extend-exemption-deadline-always")
class UpdateInductionScheduleStatusTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/inductions/{prisonNumber}/induction-schedule"
    private val today = LocalDate.now()
  }

  private val prisonNumber = randomValidPrisonNumber()

  @Test
  fun `should add 5 days to latest induction date when exemption removed given the induction was not overdue when the exemption was applied`() {
    // Given
    createInductionSchedule(
      prisonNumber,
      status = InductionScheduleStatusEntity.EXEMPT_PRISONER_FAILED_TO_ENGAGE,
      deadlineDate = today, // induction is not overdue
    )

    val expectedDeadlineDate = today.plusDays(5)

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateInductionScheduleStatusRequest(
          prisonId = "MDI",
          status = InductionScheduleStatus.SCHEDULED,
        ),
      )
      .bearerToken(aValidTokenWithAuthority(INDUCTIONS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val inductionSchedule = inductionScheduleRepository.findByPrisonNumber(prisonNumber)
    assertThat(inductionSchedule).isNotNull
    assertThat(inductionSchedule?.scheduleStatus?.name).isEqualTo(InductionScheduleStatus.SCHEDULED.name)
    assertThat(inductionSchedule?.deadlineDate).isEqualTo(expectedDeadlineDate)
  }

  @Test
  fun `should add 5 days to latest induction date when exemption removed given the induction was already overdue when the exemption was applied`() {
    // Given
    createInductionSchedule(
      prisonNumber,
      status = InductionScheduleStatusEntity.EXEMPT_PRISONER_FAILED_TO_ENGAGE,
      deadlineDate = today.minusDays(1), // induction is already overdue
    )

    val expectedDeadlineDate = today.plusDays(5)

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateInductionScheduleStatusRequest(
          prisonId = "MDI",
          status = InductionScheduleStatus.SCHEDULED,
        ),
      )
      .bearerToken(aValidTokenWithAuthority(INDUCTIONS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val inductionSchedule = inductionScheduleRepository.findByPrisonNumber(prisonNumber)
    assertThat(inductionSchedule).isNotNull
    assertThat(inductionSchedule?.scheduleStatus?.name).isEqualTo(InductionScheduleStatus.SCHEDULED.name)
    assertThat(inductionSchedule?.deadlineDate).isEqualTo(expectedDeadlineDate)
  }

  @Test
  fun `should add 10 days to latest induction date when exclusion removed given the induction was not overdue when the exclusion was applied`() {
    // Given
    createInductionSchedule(
      prisonNumber,
      status = InductionScheduleStatusEntity.EXEMPT_PRISON_REGIME_CIRCUMSTANCES,
      deadlineDate = today, // induction is not overdue
    )

    val expectedDeadlineDate = today.plusDays(10)

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateInductionScheduleStatusRequest(
          prisonId = "MDI",
          status = InductionScheduleStatus.SCHEDULED,
        ),
      )
      .bearerToken(aValidTokenWithAuthority(INDUCTIONS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val inductionSchedule = inductionScheduleRepository.findByPrisonNumber(prisonNumber)
    assertThat(inductionSchedule).isNotNull
    assertThat(inductionSchedule?.scheduleStatus?.name).isEqualTo(InductionScheduleStatus.SCHEDULED.name)
    assertThat(inductionSchedule?.deadlineDate).isEqualTo(expectedDeadlineDate)
  }

  @Test
  fun `should add 10 days to latest induction date when exclusion removed given the induction was already overdue when the exclusion was applied`() {
    // Given
    createInductionSchedule(
      prisonNumber,
      status = InductionScheduleStatusEntity.EXEMPT_PRISON_REGIME_CIRCUMSTANCES,
      deadlineDate = today.minusDays(1), // induction is already overdue
    )

    val expectedDeadlineDate = today.plusDays(10)

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateInductionScheduleStatusRequest(
          prisonId = "MDI",
          status = InductionScheduleStatus.SCHEDULED,
        ),
      )
      .bearerToken(aValidTokenWithAuthority(INDUCTIONS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val inductionSchedule = inductionScheduleRepository.findByPrisonNumber(prisonNumber)
    assertThat(inductionSchedule).isNotNull
    assertThat(inductionSchedule?.scheduleStatus?.name).isEqualTo(InductionScheduleStatus.SCHEDULED.name)
    assertThat(inductionSchedule?.deadlineDate).isEqualTo(expectedDeadlineDate)
  }
}
