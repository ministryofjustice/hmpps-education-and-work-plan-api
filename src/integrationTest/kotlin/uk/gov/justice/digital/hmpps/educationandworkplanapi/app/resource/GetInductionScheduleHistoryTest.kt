package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus.COMPLETED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus.EXEMPT_PRISONER_SAFETY_ISSUES
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionSchedulesResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerNotLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import java.time.LocalDate
import java.time.OffsetDateTime

class GetInductionScheduleHistoryTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/inductions/{prisonNumber}/induction-schedule/history"
  }

  @Test
  fun `should return induction schedule history for given prison number`() {
    // Given
    val initialDateTime = OffsetDateTime.now().minusSeconds(1)
    val randomPrisonNumber = randomValidPrisonNumber()
    createInductionScheduleHistory(
      prisonNumber = randomPrisonNumber,
      status = SCHEDULED,
      version = 1,
    )
    createInductionScheduleHistory(
      prisonNumber = randomPrisonNumber,
      status = EXEMPT_PRISONER_SAFETY_ISSUES,
      version = 2,
    )
    createInductionScheduleHistory(
      prisonNumber = randomPrisonNumber,
      status = COMPLETED,
      version = 3,
    )
    createInduction(randomPrisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork())

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, randomPrisonNumber)
      .bearerToken(
        aValidTokenWithAuthority(
          INDUCTIONS_RO,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(InductionSchedulesResponse::class.java)

    // Then
    val inductionSchedulesResponse = response.responseBody.blockFirst()

    assertThat(inductionSchedulesResponse).isNotNull
    val inductionSchedules = inductionSchedulesResponse?.inductionSchedules
    assertThat(inductionSchedules).size().isEqualTo(3)
    assertThat(inductionSchedules?.get(0)).isNotNull
    assertThat(inductionSchedules?.get(0))
      .wasCreatedAtOrAfter(initialDateTime)
      .wasUpdatedAtOrAfter(initialDateTime)
      .wasCreatedBy("auser_gen")
      .wasCreatedByDisplayName("Albert User")
      .wasUpdatedBy("auser_gen")
      .wasUpdatedByDisplayName("Albert User")
      .wasScheduleCalculationRule(InductionScheduleCalculationRule.NEW_PRISON_ADMISSION)
      .wasStatus(InductionScheduleStatus.COMPLETED)
      .wasVersion(3)
      .wasInductionPerformedBy("Albert User")
      .wasInductionPerformedAt(LocalDate.now())

    assertThat(inductionSchedules?.get(1)).isNotNull
    assertThat(inductionSchedules?.get(1))
      .wasCreatedAtOrAfter(initialDateTime)
      .wasUpdatedAtOrAfter(initialDateTime)
      .wasCreatedBy("auser_gen")
      .wasCreatedByDisplayName("Albert User")
      .wasUpdatedBy("auser_gen")
      .wasUpdatedByDisplayName("Albert User")
      .wasScheduleCalculationRule(InductionScheduleCalculationRule.NEW_PRISON_ADMISSION)
      .wasStatus(InductionScheduleStatus.EXEMPT_PRISONER_SAFETY_ISSUES)
      .wasVersion(2)

    assertThat(inductionSchedules?.get(2)).isNotNull
    assertThat(inductionSchedules?.get(2))
      .wasCreatedAtOrAfter(initialDateTime)
      .wasUpdatedAtOrAfter(initialDateTime)
      .wasCreatedBy("auser_gen")
      .wasCreatedByDisplayName("Albert User")
      .wasUpdatedBy("auser_gen")
      .wasUpdatedByDisplayName("Albert User")
      .wasScheduleCalculationRule(InductionScheduleCalculationRule.NEW_PRISON_ADMISSION)
      .wasStatus(InductionScheduleStatus.SCHEDULED)
      .wasVersion(1)
  }
}
