package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus.COMPLETED
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS
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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.ADMISSION
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TRANSFERRED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.CreateInitialReviewScheduleMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.EducationAssessmentEventService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.PrisonerSearchApiService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ScheduleAdapter
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
class PrisonerReceivedIntoPrisonEventServiceTest {
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

  @Mock
  private lateinit var educationAssessmentEventService: EducationAssessmentEventService

  private val clock: Clock = Clock.fixed(Instant.parse("2026-07-23T00:00:00Z"), ZoneOffset.UTC)
  private val today: LocalDate = LocalDate.now(clock)

  private val objectMapper = ObjectMapper()

  @BeforeEach
  fun setUp() {
    eventService = PrisonerReceivedIntoPrisonEventService(
      inductionScheduleService = inductionScheduleService,
      reviewScheduleService = reviewScheduleService,
      prisonerSearchApiService = prisonerSearchApiService,
      createInitialReviewScheduleMapper = createInitialReviewScheduleMapper,
      inductionService = inductionService,
      actionPlanService = actionPlanService,
      scheduleAdapter = scheduleAdapter,
      educationAssessmentEventService = educationAssessmentEventService,
      clock = clock,
      ciagKpiProcessingRule = "PEF",
    )
  }

  @Test
  fun `should process event given reason is prisoner admission and prisoner does not already have an Induction and Induction Schedule`() {
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
    given(prisonerSearchApiService.getPrisoner(prisonNumber)).willReturn(prisoner)

    given(inductionService.getInductionForPrisoner(prisonNumber)).willThrow(InductionNotFoundException(prisonNumber))

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(inductionScheduleService).createInductionSchedule(
      prisonNumber,
      prisonerAdmissionDate,
      prisonId,
    )
    verifyNoInteractions(reviewScheduleService)
    verifyNoInteractions(scheduleAdapter)
  }

  @Test
  fun `should schedule induction on admission given prisoner has no Induction Schedule and their assessments are already complete`() {
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
    given(prisonerSearchApiService.getPrisoner(prisonNumber)).willReturn(prisoner)
    given(inductionService.getInductionForPrisoner(prisonNumber)).willThrow(InductionNotFoundException(prisonNumber))
    given(educationAssessmentEventService.hasCompletedAllAssessments(prisonNumber)).willReturn(true)

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(inductionScheduleService).createInductionSchedule(prisonNumber, prisonerAdmissionDate, prisonId)
    verify(inductionScheduleService).schedulePendingInductionSchedule(prisonNumber, prisonId)
  }

  @Test
  fun `should not schedule induction on admission given prisoner has no Induction Schedule and their assessments are not complete`() {
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
    given(prisonerSearchApiService.getPrisoner(prisonNumber)).willReturn(prisoner)
    given(inductionService.getInductionForPrisoner(prisonNumber)).willThrow(InductionNotFoundException(prisonNumber))
    given(educationAssessmentEventService.hasCompletedAllAssessments(prisonNumber)).willReturn(false)

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(inductionScheduleService).createInductionSchedule(prisonNumber, prisonerAdmissionDate, prisonId)
    verify(inductionScheduleService, never()).schedulePendingInductionSchedule(any(), any())
  }

  @Test
  fun `should process event given reason is prisoner admission and prisoner already has an Induction and Action Plan but no Induction Schedule`() {
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
      reason = ADMISSION,
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

    given(inductionScheduleService.createInductionSchedule(any(), any(), any())).willThrow(
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
      reason = ADMISSION,
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

    given(inductionScheduleService.createInductionSchedule(any(), any(), any())).willThrow(
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
      reason = ADMISSION,
      prisonId = "BXI",
    )
    val inboundEvent = anInboundEvent(
      additionalInformation = additionalInformation,
      eventOccurredAt = prisonerAdmissionDate.atTime(11, 47, 32).toInstant(ZoneOffset.UTC),
    )

    given(inductionService.getInductionForPrisoner(prisonNumber)).willThrow(InductionNotFoundException(prisonNumber))

    val inductionSchedule = aValidInductionSchedule(prisonNumber = prisonNumber, scheduleStatus = SCHEDULED)
    given(inductionScheduleService.createInductionSchedule(any(), any(), any())).willThrow(
      InductionScheduleAlreadyExistsException(inductionSchedule),
    )

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(inductionScheduleService).createInductionSchedule(
      prisonNumber,
      prisonerAdmissionDate,
      prisonId,
    )
    verify(inductionScheduleService).reschedulePrisonersInductionSchedule(
      prisonNumber,
      prisonerAdmissionDate,
      prisonId,
      InductionScheduleCalculationRule.NEW_PRISON_ADMISSION,
    )
    verify(prisonerSearchApiService).getPrisoner(prisonNumber)
  }

  @Test
  fun `should schedule pending induction given prisoner admission and prisoner already has a PENDING Induction Schedule and their assessments are complete`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val prisonerAdmissionDate = LocalDate.now()
    val prisonId = "MDI"

    val prisoner = aValidPrisoner(prisonerNumber = prisonNumber, prisonId = prisonId)
    given(prisonerSearchApiService.getPrisoner(any())).willReturn(prisoner)

    val additionalInformation = aValidPrisonerReceivedAdditionalInformation(
      prisonNumber = prisonNumber,
      reason = ADMISSION,
      prisonId = "BXI",
    )
    val inboundEvent = anInboundEvent(
      additionalInformation = additionalInformation,
      eventOccurredAt = prisonerAdmissionDate.atTime(11, 47, 32).toInstant(ZoneOffset.UTC),
    )

    given(inductionService.getInductionForPrisoner(prisonNumber)).willThrow(InductionNotFoundException(prisonNumber))

    val inductionSchedule = aValidInductionSchedule(prisonNumber = prisonNumber, scheduleStatus = PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS)
    given(inductionScheduleService.createInductionSchedule(any(), any(), any())).willThrow(
      InductionScheduleAlreadyExistsException(inductionSchedule),
    )
    given(educationAssessmentEventService.hasCompletedAllAssessments(prisonNumber)).willReturn(true)

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(inductionScheduleService).schedulePendingInductionSchedule(prisonNumber, prisonId)
    verify(inductionScheduleService, never()).reschedulePrisonersInductionSchedule(any(), any(), any(), anyOrNull())
  }

  @Test
  fun `should leave induction pending given prisoner admission and prisoner already has a PENDING Induction Schedule and their assessments are not complete`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val prisonerAdmissionDate = LocalDate.now()
    val prisonId = "MDI"

    val prisoner = aValidPrisoner(prisonerNumber = prisonNumber, prisonId = prisonId)
    given(prisonerSearchApiService.getPrisoner(any())).willReturn(prisoner)

    val additionalInformation = aValidPrisonerReceivedAdditionalInformation(
      prisonNumber = prisonNumber,
      reason = ADMISSION,
      prisonId = "BXI",
    )
    val inboundEvent = anInboundEvent(
      additionalInformation = additionalInformation,
      eventOccurredAt = prisonerAdmissionDate.atTime(11, 47, 32).toInstant(ZoneOffset.UTC),
    )

    given(inductionService.getInductionForPrisoner(prisonNumber)).willThrow(InductionNotFoundException(prisonNumber))

    val inductionSchedule = aValidInductionSchedule(prisonNumber = prisonNumber, scheduleStatus = PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS)
    given(inductionScheduleService.createInductionSchedule(any(), any(), any())).willThrow(
      InductionScheduleAlreadyExistsException(inductionSchedule),
    )
    given(educationAssessmentEventService.hasCompletedAllAssessments(prisonNumber)).willReturn(false)

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(inductionScheduleService, never()).schedulePendingInductionSchedule(any(), any())
    verify(inductionScheduleService, never()).reschedulePrisonersInductionSchedule(any(), any(), any(), anyOrNull())
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
      reason = ADMISSION,
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

  @Test
  fun `should process event given reason is prisoner transfer`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val prisoner = aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "BXI")

    val additionalInformation = aValidPrisonerReceivedAdditionalInformation(
      prisonNumber = prisonNumber,
      reason = TRANSFERRED,
      prisonId = "BXI",
    )
    val inboundEvent = anInboundEvent(additionalInformation)

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(reviewScheduleService).exemptAndReScheduleActiveReviewScheduleDueToPrisonerTransfer(prisonNumber, "BXI")
  }

  @Test
  fun `should apply 17 day transfer rule given prisoner has completed their induction and has 17 or more days left to serve`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val releaseDate = today.plusDays(18)
    val additionalInformation = aValidPrisonerReceivedAdditionalInformation(prisonNumber = prisonNumber, reason = TRANSFERRED, prisonId = "BXI")
    val inboundEvent = anInboundEvent(additionalInformation)
    given(scheduleAdapter.isInductionComplete(prisonNumber)).willReturn(true)
    given(prisonerSearchApiService.getPrisoner(prisonNumber)).willReturn(aValidPrisoner(prisonerNumber = prisonNumber, releaseDate = releaseDate))

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(reviewScheduleService).handle17DayTransferRule(prisonNumber, "BXI", releaseDate)
  }

  @Test
  fun `should apply 17 day transfer rule given prisoner has completed their induction and has a release date in the past`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val releaseDate = today.minusDays(1)
    val additionalInformation = aValidPrisonerReceivedAdditionalInformation(prisonNumber = prisonNumber, reason = TRANSFERRED, prisonId = "BXI")
    val inboundEvent = anInboundEvent(additionalInformation)
    given(scheduleAdapter.isInductionComplete(prisonNumber)).willReturn(true)
    given(prisonerSearchApiService.getPrisoner(prisonNumber)).willReturn(aValidPrisoner(prisonerNumber = prisonNumber, releaseDate = releaseDate))

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(reviewScheduleService).handle17DayTransferRule(prisonNumber, "BXI", releaseDate)
  }

  @Test
  fun `should not apply 17 day transfer rule given prisoner has completed their induction but has less than 17 days left to serve`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val additionalInformation = aValidPrisonerReceivedAdditionalInformation(prisonNumber = prisonNumber, reason = TRANSFERRED, prisonId = "BXI")
    val inboundEvent = anInboundEvent(additionalInformation)
    given(scheduleAdapter.isInductionComplete(prisonNumber)).willReturn(true)
    given(prisonerSearchApiService.getPrisoner(prisonNumber)).willReturn(aValidPrisoner(prisonerNumber = prisonNumber, releaseDate = today.plusDays(16)))

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(reviewScheduleService, never()).handle17DayTransferRule(any(), any(), any())
  }

  @Test
  fun `should not apply 17 day transfer rule given prisoner has completed their induction but has no release date`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val additionalInformation = aValidPrisonerReceivedAdditionalInformation(prisonNumber = prisonNumber, reason = TRANSFERRED, prisonId = "BXI")
    val inboundEvent = anInboundEvent(additionalInformation)
    given(scheduleAdapter.isInductionComplete(prisonNumber)).willReturn(true)
    given(prisonerSearchApiService.getPrisoner(prisonNumber)).willReturn(aValidPrisoner(prisonerNumber = prisonNumber, releaseDate = null))

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(reviewScheduleService, never()).handle17DayTransferRule(any(), any(), any())
  }

  @Test
  fun `should not apply 17 day transfer rule given prisoner has not completed their induction`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val additionalInformation = aValidPrisonerReceivedAdditionalInformation(prisonNumber = prisonNumber, reason = TRANSFERRED, prisonId = "BXI")
    val inboundEvent = anInboundEvent(additionalInformation)
    given(scheduleAdapter.isInductionComplete(prisonNumber)).willReturn(false)

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(reviewScheduleService, never()).handle17DayTransferRule(any(), any(), any())
    verify(prisonerSearchApiService, never()).getPrisoner(prisonNumber)
  }

  @ParameterizedTest
  @CsvSource(value = ["RETURN_FROM_COURT"])
  fun `should process event but not call service given reason is not a prisoner admission reason that we should process`(
    reason: Reason,
  ) {
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
