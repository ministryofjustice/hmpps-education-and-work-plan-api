package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidUpdateInductionScheduleStatusRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus as InductionScheduleStatusEntity

@Isolated
class UpdateInductionScheduleStatusTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/inductions/{prisonNumber}/induction-schedule"
  }

  private val prisonNumber = aValidPrisonNumber()

  @Test
  fun `should fail to update induction schedule status given no data provided`() {
    // Given

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .bodyValue(
        """
          { }
        """.trimIndent(),
      )
      .bearerToken(aValidTokenWithAuthority(INDUCTIONS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(BAD_REQUEST.value())
      .hasUserMessageContaining("JSON parse error")
      .hasUserMessageContaining("value failed for JSON property prisonId due to missing (therefore NULL) value for creator parameter prisonId")
  }

  @Test
  fun `should fail to update induction status given induction schedule does not exist`() {
    // Given
    wiremockService.stubGetPrisonerNotFound(prisonNumber)

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(aValidUpdateInductionScheduleStatusRequest())
      .bearerToken(aValidTokenWithAuthority(INDUCTIONS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNotFound
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(NOT_FOUND.value())
      .hasUserMessage("Induction schedule not found for prisoner [$prisonNumber]")
  }

  @Test
  fun `should update induction schedule to exempt status`() {
    // Given
    createInductionSchedule(prisonNumber)

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateInductionScheduleStatusRequest(
          prisonId = "MDI",
          status = InductionScheduleStatus.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY,
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
    assertThat(inductionSchedule?.scheduleStatus?.name).isEqualTo(InductionScheduleStatus.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY.name)
  }

  @Test
  fun `should update induction schedule from exempt back to scheduled status`() {
    // Given
    createInductionSchedule(
      prisonNumber,
      status = InductionScheduleStatusEntity.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY,
    )

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
  }

  @Test
  fun `should fail to update induction schedule from exempt to exempt status`() {
    // Given
    createInductionSchedule(
      prisonNumber,
      status = InductionScheduleStatusEntity.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY,
    )

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateInductionScheduleStatusRequest(
          prisonId = "MDI",
          status = InductionScheduleStatus.EXEMPT_PRISONER_FAILED_TO_ENGAGE,
        ),
      )
      .bearerToken(aValidTokenWithAuthority(INDUCTIONS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .is4xxClientError
      .returnResult(ErrorResponse::class.java)

    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(CONFLICT.value())
      .hasUserMessage("Invalid Induction Schedule status transition for prisoner [$prisonNumber] status from EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY to EXEMPT_PRISONER_FAILED_TO_ENGAGE")
  }

  @Test
  fun `should fail to update induction schedule from exempt to COMPLETED`() {
    // Given
    createInductionSchedule(
      prisonNumber,
      status = InductionScheduleStatusEntity.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY,
    )

    // When
    val response = webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateInductionScheduleStatusRequest(
          prisonId = "MDI",
          status = InductionScheduleStatus.COMPLETED,
        ),
      )
      .bearerToken(aValidTokenWithAuthority(INDUCTIONS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .is4xxClientError
      .returnResult(ErrorResponse::class.java)

    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(CONFLICT.value())
      .hasUserMessage("Invalid Induction Schedule status transition for prisoner [$prisonNumber] status from EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY to COMPLETED")
  }

  @Test
  fun `should update induction schedule from Scheduled to technical issue`() {
    // Given
    createInductionSchedule(prisonNumber, status = InductionScheduleStatusEntity.SCHEDULED)

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateInductionScheduleStatusRequest(
          prisonId = "MDI",
          status = InductionScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE,
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
  }

  @Test
  fun `when technical issue should add 5 days to latest induction date `() {
    // Given
    createInductionSchedule(
      prisonNumber,
      status = InductionScheduleStatusEntity.SCHEDULED,
      deadlineDate = LocalDate.now(),
    )

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateInductionScheduleStatusRequest(
          prisonId = "MDI",
          status = InductionScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE,
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
    assertThat(inductionSchedule?.scheduleStatus?.name).isEqualTo(InductionScheduleStatus.SCHEDULED?.name)
    assertThat(inductionSchedule?.deadlineDate).isEqualTo(LocalDate.now().plusDays(5))
  }

  @Test
  fun `when exception removed should add 5 days to latest induction date `() {
    // Given
    createInductionSchedule(
      prisonNumber,
      status = InductionScheduleStatusEntity.EXEMPT_PRISONER_FAILED_TO_ENGAGE,
      deadlineDate = LocalDate.now(),
    )

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
    assertThat(inductionSchedule?.scheduleStatus?.name).isEqualTo(InductionScheduleStatus.SCHEDULED?.name)
    assertThat(inductionSchedule?.deadlineDate).isEqualTo(LocalDate.now().plusDays(5))
  }

  @Test
  fun `when exclusion removed should add 10 days to latest induction date `() {
    // Given
    createInductionSchedule(
      prisonNumber,
      status = InductionScheduleStatusEntity.EXEMPT_PRISON_REGIME_CIRCUMSTANCES,
      deadlineDate = LocalDate.now(),
    )

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
    assertThat(inductionSchedule?.scheduleStatus?.name).isEqualTo(InductionScheduleStatus.SCHEDULED?.name)
    assertThat(inductionSchedule?.deadlineDate).isEqualTo(LocalDate.now().plusDays(10))
  }

  @Test
  fun `Test follow on events occur`() {
    // Given
    val today = LocalDate.now()
    val fiveDaysAgo = today.minusDays(5)

    // Create a induction schedule record, scheduled to be completed by today
    createInductionSchedule(
      prisonNumber,
      deadlineDate = today,
    )

    with(inductionScheduleRepository.findByPrisonNumber(prisonNumber)) {
      assertThat(this).isNotNull
      assertThat(this?.scheduleStatus?.name).isEqualTo(InductionScheduleStatus.SCHEDULED.name)
      assertThat(this?.deadlineDate).isEqualTo(today)
    }

    // When
    // Update the schedule to exempted with an optional reason
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateInductionScheduleStatusRequest(
          prisonId = "MDI",
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.EXEMPT_PRISON_REGIME_CIRCUMSTANCES,
          exemptionReason = "Something happened in the prison which meant we could not do the induction today",
        ),
      )
      .bearerToken(aValidTokenWithAuthority(INDUCTIONS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    with(inductionScheduleRepository.findByPrisonNumber(prisonNumber)) {
      assertThat(this).isNotNull
      assertThat(this?.scheduleStatus?.name).isEqualTo(InductionScheduleStatus.EXEMPT_PRISON_REGIME_CIRCUMSTANCES.name)
      assertThat(this?.deadlineDate).isEqualTo(today)
    }

    await.untilAsserted {
      val timeline = getTimeline(prisonNumber)
      uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.timeline.assertThat(timeline)
        .hasNumberOfEvents(1)
        .event(1) {
          it.hasEventType(TimelineEventType.INDUCTION_SCHEDULE_STATUS_UPDATED)
            .hasContextualInfo(
              mapOf(
                "INDUCTION_SCHEDULE_DEADLINE_NEW" to today.toString(),
                "INDUCTION_SCHEDULE_DEADLINE_OLD" to today.toString(),
                "INDUCTION_SCHEDULE_STATUS_NEW" to "EXEMPT_PRISON_REGIME_CIRCUMSTANCES",
                "INDUCTION_SCHEDULE_STATUS_OLD" to "SCHEDULED",
                "INDUCTION_SCHEDULE_EXEMPTION_REASON" to "Something happened in the prison which meant we could not do the induction today",
              ),
            )
        }
      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)
      verify(telemetryClient).trackEvent(
        eq("INDUCTION_SCHEDULE_STATUS_UPDATED"),
        capture(eventPropertiesCaptor),
        isNull(),
      )
    }

    // Now update the schedule again to clear the exemption
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateInductionScheduleStatusRequest(
          prisonId = "MDI",
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.SCHEDULED,
        ),
      )
      .bearerToken(aValidTokenWithAuthority(INDUCTIONS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent

    val todayPlusTen = today.plusDays(10).toString()
    await.untilAsserted {
      val timeline = getTimeline(prisonNumber)
      uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.timeline.assertThat(timeline)
        .hasNumberOfEvents(2)
        .event(2) {
          it.hasEventType(TimelineEventType.INDUCTION_SCHEDULE_STATUS_UPDATED)
            .hasContextualInfo(
              mapOf(
                "INDUCTION_SCHEDULE_DEADLINE_NEW" to todayPlusTen,
                "INDUCTION_SCHEDULE_DEADLINE_OLD" to today.toString(),
                "INDUCTION_SCHEDULE_STATUS_OLD" to "EXEMPT_PRISON_REGIME_CIRCUMSTANCES",
                "INDUCTION_SCHEDULE_STATUS_NEW" to "SCHEDULED",
              ),
            )
        }
      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)
      verify(telemetryClient, times(2)).trackEvent(
        eq("INDUCTION_SCHEDULE_STATUS_UPDATED"),
        capture(eventPropertiesCaptor),
        isNull(),
      )
    }

    // test that outbound events were also created
    val inductionScheduleEvents = inductionScheduleEventQueue.receiveEventsOnQueue(QueueType.INDUCTION)
    assertThat(inductionScheduleEvents[0].personReference.identifiers[0].value).isEqualTo(prisonNumber)
    assertThat(inductionScheduleEvents[0].detailUrl)
      .isEqualTo("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
    assertThat(inductionScheduleEvents[1].personReference.identifiers[0].value).isEqualTo(prisonNumber)
    assertThat(inductionScheduleEvents[1].detailUrl)
      .isEqualTo("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
  }
}
