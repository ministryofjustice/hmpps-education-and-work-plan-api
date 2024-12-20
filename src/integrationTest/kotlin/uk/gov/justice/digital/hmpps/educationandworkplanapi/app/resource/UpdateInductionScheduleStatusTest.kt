package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
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
  fun `should fail to update review status given review schedule does not exist`() {
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
          status = InductionScheduleStatus.COMPLETE,
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
      .hasUserMessage("Invalid Induction Schedule status transition for prisoner [$prisonNumber] status from EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY to COMPLETE")
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
}
