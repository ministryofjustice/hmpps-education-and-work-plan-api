package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus.COMPLETED
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.aValidCreateInitialReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.ADMISSION
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TRANSFERRED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.CreateInitialReviewScheduleMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.PrisonerSearchApiService
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
class PrisonerReceivedIntoPrisonEventServiceTest {
  @InjectMocks
  private lateinit var eventService: PrisonerReceivedIntoPrisonEventService

  @Mock
  private lateinit var inductionScheduleService: InductionScheduleService

  @Mock
  private lateinit var reviewScheduleService: ReviewScheduleService

  @Mock
  private lateinit var prisonerSearchApiService: PrisonerSearchApiService

  @Mock
  private lateinit var createInitialReviewScheduleMapper: CreateInitialReviewScheduleMapper

  private val objectMapper = ObjectMapper()

  @Test
  fun `should process event given reason is prisoner admission and prisoner does not already have an Induction Schedule`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val prisonerAdmissionDate = LocalDate.now()
    val prisonId = "BXI"

    val additionalInformation = aValidPrisonerReceivedAdditionalInformation(
      prisonNumber = prisonNumber,
      reason = ADMISSION,
      prisonId = prisonId,
    )
    val inboundEvent = anInboundEvent(
      additionalInformation = additionalInformation,
      eventOccurredAt = prisonerAdmissionDate.atTime(11, 47, 32).toInstant(ZoneOffset.UTC),
    )

    val prisoner = aValidPrisoner(prisonNumber)
    // When
    whenever(prisonerSearchApiService.getPrisoner(prisonNumber)).thenReturn(prisoner)
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(inductionScheduleService).createInductionSchedule(prisonNumber, prisonerAdmissionDate, prisonId)
    verifyNoInteractions(reviewScheduleService)
  }

  @Test
  fun `should process event given reason is prisoner admission and prisoner already has an Induction Schedule that is COMPLETED but has no active Review Schedule`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val prisonerAdmissionDate = LocalDate.now()
    val prisonId = "BXI"

    val additionalInformation = aValidPrisonerReceivedAdditionalInformation(
      prisonNumber = prisonNumber,
      reason = ADMISSION,
      prisonId = prisonId,
    )
    val inboundEvent = anInboundEvent(
      additionalInformation = additionalInformation,
      eventOccurredAt = prisonerAdmissionDate.atTime(11, 47, 32).toInstant(ZoneOffset.UTC),
    )

    val inductionSchedule = aValidInductionSchedule(prisonNumber = prisonNumber, scheduleStatus = COMPLETED)
    given(inductionScheduleService.createInductionSchedule(any(), any(), any())).willThrow(
      InductionScheduleAlreadyExistsException(inductionSchedule),
    )

    given(reviewScheduleService.getActiveReviewScheduleForPrisoner(any())).willThrow(
      ReviewScheduleNotFoundException(prisonNumber),
    )

    val prisoner = aValidPrisoner(prisonNumber)
    given(prisonerSearchApiService.getPrisoner(prisonNumber)).willReturn(prisoner)

    val createReviewScheduleDto = aValidCreateInitialReviewScheduleDto()
    given(createInitialReviewScheduleMapper.fromPrisonerToDomain(any(), any(), any())).willReturn(createReviewScheduleDto)

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(inductionScheduleService).createInductionSchedule(prisonNumber, prisonerAdmissionDate, prisonId)
    verify(reviewScheduleService).getActiveReviewScheduleForPrisoner(prisonNumber)
    verify(prisonerSearchApiService).getPrisoner(prisonNumber)
    verify(createInitialReviewScheduleMapper).fromPrisonerToDomain(prisoner, isTransfer = false, isReadmission = true)
    verify(reviewScheduleService).createInitialReviewSchedule(createReviewScheduleDto)
  }

  @Test
  fun `should process event given reason is prisoner admission and prisoner already has an Induction Schedule that is not COMPLETED`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val prisonerAdmissionDate = LocalDate.now()

    val additionalInformation = aValidPrisonerReceivedAdditionalInformation(
      prisonNumber = prisonNumber,
      reason = ADMISSION,
      prisonId = "BXI",
    )
    val inboundEvent = anInboundEvent(
      additionalInformation = additionalInformation,
      eventOccurredAt = prisonerAdmissionDate.atTime(11, 47, 32).toInstant(ZoneOffset.UTC),
    )

    val inductionSchedule = aValidInductionSchedule(prisonNumber = prisonNumber, scheduleStatus = SCHEDULED)
    given(inductionScheduleService.createInductionSchedule(any(), any())).willThrow(
      InductionScheduleAlreadyExistsException(inductionSchedule),
    )

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(inductionScheduleService).createInductionSchedule(prisonNumber, prisonerAdmissionDate)
    verify(inductionScheduleService).reschedulePrisonersInductionSchedule(prisonNumber, prisonerAdmissionDate)
  }

  @Test
  fun `should process event given reason is prisoner transfer`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()

    val additionalInformation = aValidPrisonerReceivedAdditionalInformation(
      prisonNumber = prisonNumber,
      reason = TRANSFERRED,
      prisonId = "BXI",
    )
    val inboundEvent = anInboundEvent(additionalInformation)

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(reviewScheduleService).exemptAndReScheduleActiveReviewScheduleStatusDueToPrisonerTransfer(prisonNumber, "BXI")
  }

  @ParameterizedTest
  @CsvSource(value = ["TEMPORARY_ABSENCE_RETURN", "RETURN_FROM_COURT"])
  fun `should process event but not call service given reason is not a prisoner admission reason that we should process`(reason: Reason) {
    // Given
    val additionalInformation = aValidPrisonerReceivedAdditionalInformation(reason = reason)
    val inboundEvent = anInboundEvent(additionalInformation)

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verifyNoInteractions(inductionScheduleService)
    verifyNoInteractions(reviewScheduleService)
  }

  private fun anInboundEvent(
    additionalInformation: PrisonerReceivedAdditionalInformation,
    eventOccurredAt: Instant = Instant.now(),
  ): InboundEvent =
    InboundEvent(
      eventType = EventType.PRISONER_RECEIVED_INTO_PRISON,
      personReference = PersonReference(listOf(Identifier(type = "noms", value = "A1234BC"))),
      occurredAt = eventOccurredAt,
      publishedAt = Instant.now(),
      description = "Prisoner received into prison event",
      version = "1.0",
      additionalInformation = objectMapper.writeValueAsString(additionalInformation),
    )
}
