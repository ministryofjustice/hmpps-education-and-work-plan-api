package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus.COMPLETED
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aFullyPopulatedInduction
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidInductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto.aValidCreateInitialReviewScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidActionPlan
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.ActionPlanService
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TEMPORARY_ABSENCE_RETURN
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.CreateInitialReviewScheduleMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.PrisonerSearchApiService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ScheduleAdapter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
class PrisonerTemporaryAbsenceReturnEventServiceTest {
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

  @Mock
  private lateinit var inductionService: InductionService

  @Mock
  private lateinit var actionPlanService: ActionPlanService

  @Mock
  private lateinit var scheduleAdapter: ScheduleAdapter

  private val objectMapper = ObjectMapper()

  @Test
  fun `should process event given reason is prisoner admission and prisoner does not already have an Induction and Induction Schedule`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val prisonerAdmissionDate = LocalDate.now()
    val prisonId = "BXI"

    val additionalInformation = aValidPrisonerReceivedAdditionalInformation(
      prisonNumber = prisonNumber,
      reason = TEMPORARY_ABSENCE_RETURN,
      prisonId = prisonId,
    )
    val inboundEvent = anInboundEvent(
      additionalInformation = additionalInformation,
      eventOccurredAt = prisonerAdmissionDate.atTime(11, 47, 32).toInstant(ZoneOffset.UTC),
    )

    val prisoner = aValidPrisoner(prisonNumber)
    given(prisonerSearchApiService.getPrisoner(prisonNumber)).willReturn(prisoner)

    given(inductionService.getInductionForPrisoner(prisonNumber)).willThrow(InductionNotFoundException(prisonNumber))

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(inductionScheduleService).createInductionSchedule(
      prisonNumber,
      prisonerAdmissionDate,
      prisonId,
      releaseDate = prisoner.releaseDate,
    )
    verifyNoInteractions(reviewScheduleService)
    verifyNoInteractions(scheduleAdapter)
  }

  @Test
  fun `should process event given reason is prisoner admission and prisoner already has an Induction and Action Plan but no Induction Schedule`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val prisonerAdmissionDate = LocalDate.now()
    val prisonId = "BXI"

    val additionalInformation = aValidPrisonerReceivedAdditionalInformation(
      prisonNumber = prisonNumber,
      reason = TEMPORARY_ABSENCE_RETURN,
      prisonId = prisonId,
    )
    val inboundEvent = anInboundEvent(
      additionalInformation = additionalInformation,
      eventOccurredAt = prisonerAdmissionDate.atTime(11, 47, 32).toInstant(ZoneOffset.UTC),
    )

    val prisoner = aValidPrisoner(prisonNumber)
    given(prisonerSearchApiService.getPrisoner(prisonNumber)).willReturn(prisoner)

    given(inductionService.getInductionForPrisoner(prisonNumber)).willReturn(aFullyPopulatedInduction(prisonNumber = prisonNumber))
    given(actionPlanService.getActionPlan(prisonNumber)).willReturn(aValidActionPlan(prisonNumber = prisonNumber))
    given(inductionScheduleService.getInductionScheduleForPrisoner(prisonNumber)).willThrow(
      InductionScheduleNotFoundException(prisonNumber),
    )

    val createReviewScheduleDto = aValidCreateInitialReviewScheduleDto()
    given(createInitialReviewScheduleMapper.fromPrisonerToDomain(any(), any(), any())).willReturn(
      createReviewScheduleDto,
    )

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verifyNoMoreInteractions(inductionScheduleService)
    verify(reviewScheduleService).getActiveReviewScheduleForPrisoner(prisonNumber)
    verify(prisonerSearchApiService).getPrisoner(prisonNumber)
    verify(createInitialReviewScheduleMapper).fromPrisonerToDomain(prisoner, isTransfer = false, isReadmission = true)
    verify(reviewScheduleService).createInitialReviewSchedule(createReviewScheduleDto)
  }

  @Test
  fun `should process event given reason is prisoner admission and prisoner already has an Induction Schedule that is COMPLETED but has no active Review Schedule`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val prisonerAdmissionDate = LocalDate.now()
    val prisonId = "BXI"

    val additionalInformation = aValidPrisonerReceivedAdditionalInformation(
      prisonNumber = prisonNumber,
      reason = TEMPORARY_ABSENCE_RETURN,
      prisonId = prisonId,
    )
    val inboundEvent = anInboundEvent(
      additionalInformation = additionalInformation,
      eventOccurredAt = prisonerAdmissionDate.atTime(11, 47, 32).toInstant(ZoneOffset.UTC),
    )

    given(inductionService.getInductionForPrisoner(prisonNumber)).willReturn(aFullyPopulatedInduction(prisonNumber = prisonNumber))
    given(actionPlanService.getActionPlan(prisonNumber)).willReturn(aValidActionPlan(prisonNumber = prisonNumber))

    val inductionSchedule = aValidInductionSchedule(prisonNumber = prisonNumber, scheduleStatus = COMPLETED)
    given(inductionScheduleService.getInductionScheduleForPrisoner(prisonNumber)).willReturn(inductionSchedule)

    given(inductionScheduleService.createInductionSchedule(any(), any(), any(), any(), anyOrNull(), any())).willThrow(
      InductionScheduleAlreadyExistsException(inductionSchedule),
    )

    given(reviewScheduleService.getActiveReviewScheduleForPrisoner(any())).willThrow(
      ReviewScheduleNotFoundException(prisonNumber),
    )

    val prisoner = aValidPrisoner(prisonNumber)
    given(prisonerSearchApiService.getPrisoner(prisonNumber)).willReturn(prisoner)

    val createReviewScheduleDto = aValidCreateInitialReviewScheduleDto()
    given(createInitialReviewScheduleMapper.fromPrisonerToDomain(any(), any(), any())).willReturn(
      createReviewScheduleDto,
    )

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(inductionScheduleService).createInductionSchedule(
      prisonNumber,
      prisonerAdmissionDate,
      prisonId,
      releaseDate = prisoner.releaseDate,
    )
    verify(reviewScheduleService).getActiveReviewScheduleForPrisoner(prisonNumber)
    verify(prisonerSearchApiService).getPrisoner(prisonNumber)
    verify(createInitialReviewScheduleMapper).fromPrisonerToDomain(prisoner, isTransfer = false, isReadmission = true)
    verify(reviewScheduleService).createInitialReviewSchedule(createReviewScheduleDto)
  }

  // Weird edge case scenario as we would expect a previously released/transferred prisoner who is being re-admitted to have had their Review Schedules exempted due to release or transfer via the prisoner.release listener
  // This scenario is to cover the edge case that the Review Schedule did not get exempted for some reason when the prisoner was released, and was left "hanging"
  // In this scenario the Review Schedule is marked as EXEMPT_UNKNOWN and then a new Review Schedule is created.
  @Test
  fun `should process event given reason is prisoner admission and prisoner already has an Induction Schedule that is COMPLETED and has an active Review Schedule`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val prisonerAdmissionDate = LocalDate.now()
    val prisonId = "BXI"

    val additionalInformation = aValidPrisonerReceivedAdditionalInformation(
      prisonNumber = prisonNumber,
      reason = TEMPORARY_ABSENCE_RETURN,
      prisonId = prisonId,
    )
    val inboundEvent = anInboundEvent(
      additionalInformation = additionalInformation,
      eventOccurredAt = prisonerAdmissionDate.atTime(11, 47, 32).toInstant(ZoneOffset.UTC),
    )

    given(inductionService.getInductionForPrisoner(prisonNumber)).willReturn(aFullyPopulatedInduction(prisonNumber = prisonNumber))
    given(actionPlanService.getActionPlan(prisonNumber)).willReturn(aValidActionPlan(prisonNumber = prisonNumber))

    val inductionSchedule = aValidInductionSchedule(prisonNumber = prisonNumber, scheduleStatus = COMPLETED)
    given(inductionScheduleService.getInductionScheduleForPrisoner(prisonNumber)).willReturn(inductionSchedule)

    given(inductionScheduleService.createInductionSchedule(any(), any(), any(), any(), anyOrNull(), any())).willThrow(
      InductionScheduleAlreadyExistsException(inductionSchedule),
    )

    val reviewSchedule =
      aValidReviewSchedule(prisonNumber = prisonNumber, scheduleStatus = ReviewScheduleStatus.SCHEDULED)
    given(reviewScheduleService.getActiveReviewScheduleForPrisoner(any())).willReturn(reviewSchedule)

    val prisoner = aValidPrisoner(prisonNumber)
    given(prisonerSearchApiService.getPrisoner(prisonNumber)).willReturn(prisoner)

    val createReviewScheduleDto = aValidCreateInitialReviewScheduleDto()
    given(createInitialReviewScheduleMapper.fromPrisonerToDomain(any(), any(), any())).willReturn(
      createReviewScheduleDto,
    )

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(inductionScheduleService).createInductionSchedule(
      prisonNumber,
      prisonerAdmissionDate,
      prisonId,
      releaseDate = prisoner.releaseDate,
    )
    verify(reviewScheduleService).getActiveReviewScheduleForPrisoner(prisonNumber)
    verify(prisonerSearchApiService).getPrisoner(prisonNumber)
    verify(reviewScheduleService).exemptActiveReviewScheduleStatusDueToUnknownReason(prisonNumber, prisonId)
    verify(createInitialReviewScheduleMapper).fromPrisonerToDomain(prisoner, isTransfer = false, isReadmission = true)
    verify(reviewScheduleService).createInitialReviewSchedule(createReviewScheduleDto)
  }

  @Test
  fun `should process event given reason is prisoner admission and prisoner does not have an Induction and already has an Induction Schedule that is not COMPLETED`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val prisonerAdmissionDate = LocalDate.now()
    val prisonId = "MDI"

    val prisoner = aValidPrisoner(prisonerNumber = prisonNumber, prisonId = prisonId)
    given(prisonerSearchApiService.getPrisoner(any())).willReturn(prisoner)

    val additionalInformation = aValidPrisonerReceivedAdditionalInformation(
      prisonNumber = prisonNumber,
      reason = TEMPORARY_ABSENCE_RETURN,
      prisonId = "BXI",
    )
    val inboundEvent = anInboundEvent(
      additionalInformation = additionalInformation,
      eventOccurredAt = prisonerAdmissionDate.atTime(11, 47, 32).toInstant(ZoneOffset.UTC),
    )

    given(inductionService.getInductionForPrisoner(prisonNumber)).willThrow(InductionNotFoundException(prisonNumber))

    val inductionSchedule = aValidInductionSchedule(prisonNumber = prisonNumber, scheduleStatus = SCHEDULED)
    given(inductionScheduleService.createInductionSchedule(any(), any(), any(), any(), anyOrNull(), any())).willThrow(
      InductionScheduleAlreadyExistsException(inductionSchedule),
    )

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(inductionScheduleService).createInductionSchedule(
      prisonNumber,
      prisonerAdmissionDate,
      prisonId,
      releaseDate = prisoner.releaseDate,
    )
    verify(inductionScheduleService).reschedulePrisonersInductionSchedule(
      prisonNumber,
      prisonerAdmissionDate,
      prisonId,
      releaseDate = prisoner.releaseDate,
      InductionScheduleCalculationRule.NEW_PRISON_ADMISSION,
    )
    verify(prisonerSearchApiService).getPrisoner(prisonNumber)
  }

  @Test
  fun `should process event given reason is prisoner admission and prisoner already has an Induction but their Induction Schedule is not COMPLETED for some reason`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val prisonerAdmissionDate = LocalDate.now()
    val prisonId = "MDI"

    val prisoner = aValidPrisoner(prisonerNumber = prisonNumber, prisonId = prisonId)
    given(prisonerSearchApiService.getPrisoner(any())).willReturn(prisoner)

    val additionalInformation = aValidPrisonerReceivedAdditionalInformation(
      prisonNumber = prisonNumber,
      reason = TEMPORARY_ABSENCE_RETURN,
      prisonId = "BXI",
    )
    val inboundEvent = anInboundEvent(
      additionalInformation = additionalInformation,
      eventOccurredAt = prisonerAdmissionDate.atTime(11, 47, 32).toInstant(ZoneOffset.UTC),
    )

    given(inductionService.getInductionForPrisoner(prisonNumber)).willReturn(aFullyPopulatedInduction(prisonNumber = prisonNumber))
    given(actionPlanService.getActionPlan(prisonNumber)).willReturn(aValidActionPlan(prisonNumber = prisonNumber))

    val inductionSchedule = aValidInductionSchedule(prisonNumber = prisonNumber, scheduleStatus = SCHEDULED)
    given(inductionScheduleService.getInductionScheduleForPrisoner(prisonNumber)).willReturn(inductionSchedule)

    given(inductionScheduleService.updateInductionSchedule(any(), any(), anyOrNull(), any(), any(), anyOrNull()))
      .willReturn(inductionSchedule)

    val createReviewScheduleDto = aValidCreateInitialReviewScheduleDto()
    given(createInitialReviewScheduleMapper.fromPrisonerToDomain(any(), any(), any())).willReturn(
      createReviewScheduleDto,
    )

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(inductionScheduleService).updateInductionSchedule(
      inductionSchedule = inductionSchedule,
      newStatus = COMPLETED,
      prisonId = prisonId,
    )
    verifyNoMoreInteractions(inductionScheduleService)
    verify(reviewScheduleService).getActiveReviewScheduleForPrisoner(prisonNumber)
    verify(prisonerSearchApiService).getPrisoner(prisonNumber)
    verify(createInitialReviewScheduleMapper).fromPrisonerToDomain(prisoner, isTransfer = false, isReadmission = true)
    verify(reviewScheduleService).createInitialReviewSchedule(createReviewScheduleDto)
    verify(prisonerSearchApiService).getPrisoner(prisonNumber)
  }

  private fun anInboundEvent(
    additionalInformation: PrisonerReceivedAdditionalInformation,
    eventOccurredAt: Instant = Instant.now(),
  ): InboundEvent = InboundEvent(
    eventType = EventType.PRISONER_RECEIVED_INTO_PRISON,
    personReference = PersonReference(listOf(Identifier(type = "noms", value = "A1234BC"))),
    occurredAt = eventOccurredAt,
    publishedAt = Instant.now(),
    description = "Prisoner received into prison event",
    version = "1.0",
    additionalInformation = objectMapper.writeValueAsString(additionalInformation),
  )
}
