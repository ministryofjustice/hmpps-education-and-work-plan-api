package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import io.swagger.v3.oas.annotations.Hidden
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ActiveReviewScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNoReleaseDateForSentenceTypeException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionScheduleRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewScheduleRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventPublisher
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.Identifier
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.InboundEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.PersonReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.PrisonerReceivedIntoPrisonEventService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.CreateInitialReviewScheduleMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.PrisonerSearchApiService
import java.time.Instant
import java.time.LocalDate

private val log = KotlinLogging.logger {}

/**
 * Temporary controller class exposing REST API method that triggers the ETL of ReviewSchedule and InductionSchedule records
 */
@Hidden
@RestController
class ScheduleEtlController(
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val inductionRepository: InductionRepository,
  private val actionPlanRepository: ActionPlanRepository,
  private val reviewScheduleRepository: ReviewScheduleRepository,
  private val inductionScheduleRepository: InductionScheduleRepository,
  private val inductionScheduleService: InductionScheduleService,
  private val reviewScheduleService: ReviewScheduleService,
  private val createInitialReviewScheduleMapper: CreateInitialReviewScheduleMapper,
  private val eventPublisher: EventPublisher,
  private val prisonerReceivedIntoPrisonEventService: PrisonerReceivedIntoPrisonEventService,
) {

  @PostMapping("/action-plans/schedules/etl-messages")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  @Transactional
  fun generateMessages(
    @RequestParam(name = "dryRun", required = false, defaultValue = "false") dryRun: Boolean,
  ): MessagesEtlResponse {
    val inductionPrisonNumbers = inductionScheduleRepository.findAll().map { it.prisonNumber }
    val reviewPrisonNumbers = reviewScheduleRepository.findAll().map { it.prisonNumber }.toSet().toList()

    if (!dryRun) {
      inductionPrisonNumbers.forEach(eventPublisher::createAndPublishInductionEvent)
      reviewPrisonNumbers.forEach(eventPublisher::createAndPublishReviewScheduleEvent)
    }

    return MessagesEtlResponse(
      dryRun = dryRun,
      inductionSchedulePrisonerIds = inductionPrisonNumbers,
      reviewSchedulePrisonerIds = reviewPrisonNumbers,
    ).also {
      log.info(
        """
            ETL Published messages:
            Dry run: ${it.dryRun}
            Number of induction messages: ${it.numberOfInductionScheduleMessages}
            Number of review messages: ${it.numberOfReviewScheduleMessages}
        """.trimIndent(),
      )
    }
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  @PostMapping(value = ["/action-plans/schedules/etl-inductions/{prisonId}"])
  @Transactional
  fun checkInductionSchedulesForPrisonersInPrison(
    @PathVariable("prisonId") prisonId: String,
  ): InductionSchedulesEtlResponse {
    log.info("Starting ETL induction check process for prison ID: $prisonId")

    val allPrisoners = prisonerSearchApiService.getAllPrisonersInPrison(prisonId).also {
      log.info("Total prisoners in prison $prisonId: ${it.size}")
    }

    val totalPrisonersInPrison = allPrisoners.size

    val prisonersWithoutInductionSchedule = filterPrisonersWithoutInductionSchedule(allPrisoners)
    val prisonersWithInductions = filterPrisonersWithInductions(allPrisoners)

    val eligibleInductionSchedulePrisoners =
      filterPrisonersWithNoInduction(
        prisonersWithoutInductionSchedule,
        prisonersWithInductions,
      ).filter { prisoner ->
        val thresholdDate = LocalDate.now().plusDays(3)
        prisoner.releaseDate == null || prisoner.releaseDate.isAfter(thresholdDate)
      }

    // Prepare response data
    val response = InductionSchedulesEtlResponse(
      prisonId,
      eligibleInductionSchedulePrisoners.map { it.prisonerNumber },
      totalPrisonersInPrison,
    )

    log.info("Completed ETL induction check process for prison ID: $prisonId. Response: ${response.summary}")
    return response
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  @PostMapping(value = ["/action-plans/schedules/etl/{prisonId}"])
  @Transactional
  fun createSchedulesForPrisonersInPrison(
    @PathVariable("prisonId") prisonId: String,
    @RequestParam(required = false, name = "dryRun") dryRun: Boolean = false,
  ): SchedulesEtlResponse {
    log.info("Starting ETL process for prison ID: $prisonId, dryRun: $dryRun")

    val allPrisoners = prisonerSearchApiService.getAllPrisonersInPrison(prisonId).also {
      log.info("Total prisoners in prison $prisonId: ${it.size}")
    }

    val totalPrisonersInPrison = allPrisoners.size

    // Get filtered prisoners step-by-step
    val prisonersWithoutReviewSchedules = filterPrisonersWithoutReviewSchedules(allPrisoners)
    val prisonersWithoutInductionSchedule = filterPrisonersWithoutInductionSchedule(allPrisoners)
    val prisonersWithInductions = filterPrisonersWithInductions(allPrisoners)
    val prisonersWithInductionsAndNoReviewSchedule =
      filterPrisonersWithInductionButNoReviewSchedule(prisonersWithInductions, prisonersWithoutReviewSchedules)

    val eligibleInductionSchedulePrisoners =
      filterPrisonersWithNoInduction(prisonersWithoutInductionSchedule, prisonersWithInductions)
    val eligibleReviewSchedulePrisoners = filterPrisonersWithActionPlans(prisonersWithInductionsAndNoReviewSchedule)

    val totalPrisonersWithInductionSchedule = totalPrisonersInPrison - prisonersWithoutInductionSchedule.size
    val totalPrisonersWithReviewSchedule = totalPrisonersInPrison - prisonersWithoutReviewSchedules.size
    val totalPrisonersWithActionPlan = eligibleReviewSchedulePrisoners.size

    // Create schedules for eligible prisoners
    val createdReviewSchedules = mutableListOf<String>()
    val failedReviewSchedules = mutableListOf<String>()
    val createdInductionSchedules = mutableListOf<String>()
    val failedInductionSchedules = mutableListOf<String>()

    createInductionSchedules(eligibleInductionSchedulePrisoners, createdInductionSchedules, failedInductionSchedules)
    createReviewSchedules(eligibleReviewSchedulePrisoners, createdReviewSchedules, failedReviewSchedules)

    // Prepare response data
    val response = SchedulesEtlResponse(
      dryRun = dryRun,
      prisonId = prisonId,
      totalPrisonersInPrison = totalPrisonersInPrison,
      totalPrisonersWithReviewSchedule = totalPrisonersWithReviewSchedule,
      totalPrisonersWithInductionSchedule = totalPrisonersWithInductionSchedule,
      totalPrisonersWithInduction = prisonersWithInductions.size,
      totalPrisonersWithActionPlan = totalPrisonersWithActionPlan,
      eligibleInductionSchedulePrisoners = eligibleInductionSchedulePrisoners.size,
      eligibleReviewSchedulePrisoners = eligibleReviewSchedulePrisoners.size,
      prisonersWithCreatedReviewSchedules = createdReviewSchedules,
      prisonersWithoutReviewSchedules = failedReviewSchedules,
      prisonersWithCreatedInductionSchedule = createdInductionSchedules,
      prisonersWithoutInductionSchedule = failedInductionSchedules,
    )

    // Handle dry run
    if (dryRun) {
      log.info("Dry run completed. Response: ${response.summary}")
      throw SchedulesEtlRollbackException(response)
    }

    log.info("ETL process completed for prison ID: $prisonId. Response: ${response.summary}")
    return response
  }

  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  @GetMapping(value = ["/action-plans/schedules/etl-check/{prisonId}"])
  @Transactional
  fun checkSchedulesForPrisonersInPrison(
    @PathVariable("prisonId") prisonId: String,
  ): CheckSchedulesEtlResponse {
    log.info("Check ETL process for prison ID: $prisonId")

    val allPrisoners = prisonerSearchApiService.getAllPrisonersInPrison(prisonId).also {
      log.info("Total prisoners in prison $prisonId: ${it.size}")
    }

    val prisonersWithSchedules = prisonersWithAnySchedule(allPrisoners)

    // filter out the prisoners with either a review schedule or an induction schedule
    val allPrisonersWithoutSchedules = allPrisoners.map { it.prisonerNumber }
      .filterNot { it in prisonersWithSchedules }

    // Prepare response data
    val response = CheckSchedulesEtlResponse(
      prisonId = prisonId,
      totalNumberOfPrisoners = allPrisoners.size,
      prisonersWithoutPLPData = allPrisonersWithoutSchedules,
    )

    log.info("ETL check process completed for prison ID: $prisonId. Response: ${response.prisonersWithoutPLPData.size}")
    return response
  }

  /**
   * ETL job to create schedules for any prisoners in a prison that were missed in the original ETL.
   * This method uses the same message processing as if they were new prisoners.
   */
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  @PutMapping(value = ["/action-plans/schedules/etl-fix/{prisonId}"])
  @Transactional
  fun fixSchedulesForPrisonersInPrison(
    @PathVariable("prisonId") prisonId: String,
  ): CheckDataFixEtlResponse {
    log.info("Check ETL process for prison ID: $prisonId")

    val allPrisoners = prisonerSearchApiService.getAllPrisonersInPrison(prisonId).also {
      log.info("Total prisoners in prison $prisonId: ${it.size}")
    }

    val prisonersWithSchedules = prisonersWithAnySchedule(allPrisoners)

    // filter out the prisoners with either a review schedule or an induction schedule
    val allPrisonersWithoutSchedules = allPrisoners.map { it.prisonerNumber }
      .filterNot { it in prisonersWithSchedules }

    allPrisonersWithoutSchedules.forEach {
      prisonerReceivedIntoPrisonEventService.process(
        inboundEvent = inboundEvent(it, prisonId),
        additionalInformation = additionalInformation(it, prisonId),
        dataCorrection = true,
      )
      // Pause for 1 second between each iteration
      Thread.sleep(1000)
    }

    // Prepare response data
    val response = CheckDataFixEtlResponse(
      prisonId = prisonId,
      totalNumberOfPrisoners = allPrisoners.size,
      candidatePrisoners = allPrisonersWithoutSchedules,
    )

    log.info("ETL data fix process completed for prison ID: $prisonId. Response: ${response.candidatePrisoners.size}")
    return response
  }

  /**
   * ETL job to create schedules for any prisoners in a prison that were missed in the original ETL.
   * This method uses the same message processing as if they were new prisoners.
   */
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  @PutMapping(value = ["/action-plans/schedules/etl-schedule-fix/{prisonerNumber}"])
  @Transactional
  fun fixSchedulesForPrisoner(
    @PathVariable("prisonerNumber") prisonerNumber: String,
  ): String {
    log.info("Schedule fix prisonerNumber $prisonerNumber")

    prisonerReceivedIntoPrisonEventService.process(
      inboundEvent = inboundEvent(prisonerNumber),
      additionalInformation = additionalInformation(prisonerNumber),
      dataCorrection = true,
    )

    // Prepare response data
    val response = "Schedule data fix process completed for prisonerNumber ID: $prisonerNumber"
    log.info(response)
    return response
  }

  fun additionalInformation(prisonNumber: String, prisonId: String = "ABC"): PrisonerReceivedAdditionalInformation = PrisonerReceivedAdditionalInformation(
    nomsNumber = prisonNumber,
    reason = PrisonerReceivedAdditionalInformation.Reason.ADMISSION,
    details = "ACTIVE IN:ADM-N",
    currentLocation = PrisonerReceivedAdditionalInformation.Location.IN_PRISON,
    prisonId = prisonId,
    nomisMovementReasonCode = "N",
    currentPrisonStatus = PrisonerReceivedAdditionalInformation.PrisonStatus.UNDER_PRISON_CARE,
  )

  private fun inboundEvent(prisonNumber: String, prisonId: String = "ABC"): InboundEvent {
    val inboundEvent = InboundEvent(
      eventType = EventType.PRISONER_RECEIVED_INTO_PRISON,
      description = "A prisoner has been received into prison",
      personReference = PersonReference(
        identifiers = listOf(Identifier("NOMS", prisonNumber)),
      ),
      version = "1.0",
      occurredAt = Instant.now(),
      publishedAt = Instant.now(),
      additionalInformation = "{ \"nomsNumber\": \"$prisonNumber\", \"reason\": \"ADMISSION\", \"details\": \"ACTIVE IN:ADM-N\", \"currentLocation\": \"IN_PRISON\", \"prisonId\": \"$prisonId\", \"nomisMovementReasonCode\": \"N\", \"currentPrisonStatus\": \"UNDER_PRISON_CARE\" }",
    )
    return inboundEvent
  }

  private fun createReviewSchedules(
    eligibleReviewSchedulePrisoners: List<Prisoner>,
    createdReviewSchedules: MutableList<String>,
    failedReviewSchedules: MutableList<String>,
  ) {
    eligibleReviewSchedulePrisoners.forEach { prisoner ->
      val prisonNumber = prisoner.prisonerNumber
      try {
        createReviewSchedule(prisoner)
        createdReviewSchedules.add(prisonNumber)
      } catch (e: Exception) {
        handleReviewScheduleCreationError(prisonNumber, e, failedReviewSchedules)
      }
    }
  }

  private fun createInductionSchedules(
    eligibleInductionSchedulePrisoners: List<Prisoner>,
    createdInductionSchedules: MutableList<String>,
    failedInductionSchedules: MutableList<String>,
  ) {
    eligibleInductionSchedulePrisoners.forEach { prisoner ->
      if (prisoner.releaseDate != null &&
        prisoner.releaseDate.isBefore(LocalDate.now())
      ) {
        log.info { "Induction for prisoner ${prisoner.prisonerNumber} skipped due to release within 7 days of go-live." }
      } else {
        val prisonNumber = prisoner.prisonerNumber
        try {
          createInductionSchedule(prisoner)
          createdInductionSchedules.add(prisonNumber)
        } catch (e: Exception) {
          handleInductionScheduleCreationError(prisonNumber, e, failedInductionSchedules)
        }
      }
    }
  }

  private fun prisonersWithAnySchedule(prisoners: List<Prisoner>): List<String> {
    val prisonNumbers = prisoners.map { it.prisonerNumber }
    val prisonersWithReviewSchedules =
      reviewScheduleRepository.findByPrisonNumberIn(prisonNumbers).map { it.prisonNumber }.toSet()
    val prisonersWithInductionSchedules =
      inductionScheduleRepository.findByPrisonNumberIn(prisonNumbers).map { it.prisonNumber }.toSet()
    return (prisonersWithReviewSchedules + prisonersWithInductionSchedules).toSet().toList()
  }

  private fun filterPrisonersWithNoInduction(
    prisonersWithoutInductionSchedules: List<Prisoner>,
    prisonersWithInductions: List<Prisoner>,
  ): List<Prisoner> {
    val prisonerNumbersWithInductions = prisonersWithInductions.map { it.prisonerNumber }.toSet()
    return prisonersWithoutInductionSchedules.filter { it.prisonerNumber !in prisonerNumbersWithInductions }
  }

  private fun filterPrisonersWithoutReviewSchedules(prisoners: List<Prisoner>): List<Prisoner> {
    val prisonNumbers = prisoners.map { it.prisonerNumber }
    val prisonersWithReviewSchedules =
      reviewScheduleRepository.findByPrisonNumberIn(prisonNumbers).map { it.prisonNumber }.toSet()
    return prisoners.filter { it.prisonerNumber !in prisonersWithReviewSchedules }
  }

  private fun filterPrisonersWithoutInductionSchedule(prisoners: List<Prisoner>): List<Prisoner> {
    val prisonNumbers = prisoners.map { it.prisonerNumber }
    val prisonersWithInductionSchedules =
      inductionScheduleRepository.findByPrisonNumberIn(prisonNumbers).map { it.prisonNumber }.toSet()
    return prisoners.filter { it.prisonerNumber !in prisonersWithInductionSchedules }
  }

  private fun filterPrisonersWithInductions(prisoners: List<Prisoner>): List<Prisoner> {
    val prisonNumbers = prisoners.map { it.prisonerNumber }
    val prisonersWithInductions =
      inductionRepository.findByPrisonNumberIn(prisonNumbers).map { it.prisonNumber }.toSet()
    return prisoners.filter { it.prisonerNumber in prisonersWithInductions }
  }

  private fun filterPrisonersWithInductionButNoReviewSchedule(
    prisonersWithInduction: List<Prisoner>,
    prisonersWithoutReviewSchedules: List<Prisoner>,
  ): List<Prisoner> {
    val prisonNumbersWithNoReviewSchedule = prisonersWithoutReviewSchedules.map { it.prisonerNumber }
    return prisonersWithInduction.filter { it.prisonerNumber in prisonNumbersWithNoReviewSchedule }
  }

  private fun filterPrisonersWithActionPlans(prisoners: List<Prisoner>): List<Prisoner> {
    val prisonNumbers = prisoners.map { it.prisonerNumber }
    val prisonersWithActionPlans =
      actionPlanRepository.findByPrisonNumberIn(prisonNumbers).map { it.prisonNumber }.toSet()
    return prisoners.filter { it.prisonerNumber in prisonersWithActionPlans }
  }

  private fun createReviewSchedule(prisoner: Prisoner) {
    val createInitialReviewScheduleDto = createInitialReviewScheduleMapper.fromPrisonerToDomain(
      prisoner = prisoner,
      isReadmission = false,
      isTransfer = false,
    )
    reviewScheduleService.createInitialReviewSchedule(createInitialReviewScheduleDto)
  }

  private fun createInductionSchedule(prisoner: Prisoner) {
    // Needs to create an induction schedule of with a deadline 6 months out
    inductionScheduleService.createInductionSchedule(
      prisonNumber = prisoner.prisonerNumber,
      prisonerAdmissionDate = LocalDate.now(),
      prisonId = prisoner.prisonId ?: "N/A",
      newAdmission = false,
      releaseDate = prisoner.releaseDate,
      dataCorrection = false,
    )
  }

  private fun handleReviewScheduleCreationError(prisonNumber: String, e: Exception, failedList: MutableList<String>) {
    when (e) {
      is NullPointerException -> log.error("NPE creating Review Schedule for $prisonNumber: Missing releaseDate. $e")
      is ReviewScheduleNoReleaseDateForSentenceTypeException, is ActiveReviewScheduleAlreadyExistsException -> log.warn(
        "Expected exception for $prisonNumber: $e",
      )

      else -> log.error("Unexpected error creating Review Schedule for $prisonNumber: $e")
    }
    failedList.add(prisonNumber)
  }

  private fun handleInductionScheduleCreationError(
    prisonNumber: String,
    e: Exception,
    failedList: MutableList<String>,
  ) {
    when (e) {
      is NullPointerException -> log.error("NPE creating Induction Schedule for $prisonNumber $e")
      is InductionScheduleAlreadyExistsException -> log.warn(
        "Expected exception for $prisonNumber: $e",
      )

      else -> log.error("Unexpected error creating Induction Schedule for $prisonNumber: $e")
    }
    failedList.add(prisonNumber)
  }

  @ExceptionHandler(SchedulesEtlRollbackException::class)
  fun handleRollbackException(e: SchedulesEtlRollbackException): ResponseEntity<SchedulesEtlResponse> {
    log.info("Dry run rollback triggered. Response: ${e.schedulesEtlResponse.summary}")
    return ResponseEntity.ok(e.schedulesEtlResponse)
  }
}

data class CheckDataFixEtlResponse(
  val prisonId: String,
  val candidatePrisoners: List<String> = listOf(),
  val totalNumberOfPrisoners: Int,
) {

  val summary: String
    get() =
      (
        """
          Prison ID: $prisonId
          Total number of prisoners: $totalNumberOfPrisoners
          Created schedules for: ${candidatePrisoners.size}
          Prison IDs: $candidatePrisoners 
        """.trimIndent()
        )
}

data class CheckSchedulesEtlResponse(
  val prisonId: String,
  val prisonersWithoutPLPData: List<String> = listOf(),
  val totalNumberOfPrisoners: Int,
) {

  val summary: String
    get() =
      (
        """
          Prison ID: $prisonId
          Total number of prisoners: $totalNumberOfPrisoners
          Number of prisoners with no PLP schedule: ${prisonersWithoutPLPData.size}
          Prison IDs: $prisonersWithoutPLPData 
        """.trimIndent()
        )
}

data class InductionSchedulesEtlResponse(
  val prisonId: String,
  val eligiblePrisonNumbers: List<String> = listOf(),
  val totalNumberOfPrisoners: Int,
) {

  val summary: String
    get() =
      (
        """
          Prison ID: $prisonId
          Total number of prisoners: $totalNumberOfPrisoners
          Number of prisoners eligible for induction schedule: ${eligiblePrisonNumbers.size}
          Prison IDs: $eligiblePrisonNumbers 
        """.trimIndent()
        )
}

data class MessagesEtlResponse(
  val dryRun: Boolean,
  val reviewSchedulePrisonerIds: List<String> = listOf(),
  val inductionSchedulePrisonerIds: List<String> = listOf(),
) {
  val numberOfReviewScheduleMessages: Int
    get() = reviewSchedulePrisonerIds.size
  val numberOfInductionScheduleMessages: Int
    get() = inductionSchedulePrisonerIds.size
}

data class SchedulesEtlResponse(
  val dryRun: Boolean,
  val prisonId: String,
  val totalPrisonersInPrison: Int,
  val totalPrisonersWithInductionSchedule: Int,
  val totalPrisonersWithReviewSchedule: Int,
  val totalPrisonersWithInduction: Int,
  val totalPrisonersWithActionPlan: Int,
  val prisonersWithCreatedReviewSchedules: List<String>,
  val prisonersWithoutReviewSchedules: List<String>,
  val prisonersWithCreatedInductionSchedule: List<String>,
  val prisonersWithoutInductionSchedule: List<String>,
  val eligibleInductionSchedulePrisoners: Int,
  val eligibleReviewSchedulePrisoners: Int,
) {
  val summary: String
    get() =
      (
        if (dryRun) {
          """
            ***************
            *** DRY RUN ***
            ***************
    
          """.trimIndent()
        } else {
          ""
        }
        ) +
        """
          Prisoners requiring Schedules
          -------------------------------------
          Total of $totalPrisonersInPrison prisoners in $prisonId.
          Of those, $totalPrisonersWithInductionSchedule already have an Induction Schedule, 
          leaving ${totalPrisonersInPrison - totalPrisonersWithInductionSchedule} candidates for induction schedules.
          Of those, $totalPrisonersWithInduction have an Induction, 
          leaving $eligibleInductionSchedulePrisoners eligible for induction schedule creation.
          
          Total of $totalPrisonersInPrison prisoners in $prisonId.
          Of those, $totalPrisonersWithReviewSchedule already have a Review Schedule, leaving ${totalPrisonersInPrison - totalPrisonersWithReviewSchedule} candidate prisoners.
          Of those, $totalPrisonersWithInduction have an Induction, and $totalPrisonersWithActionPlan have an Induction and an Action Plan.
          leaving $eligibleReviewSchedulePrisoners eligible for review schedule creation.
          
          Created Review Schedules
          ------------------------          
          Review Schedules for ${prisonersWithoutReviewSchedules.size} prisoners were not created:
          $prisonersWithoutReviewSchedules
          
          Review Schedules for ${prisonersWithCreatedReviewSchedules.size} prisoners were successfully created:
          $prisonersWithCreatedReviewSchedules
          
          Created Induction Schedule
          ------------------------          
          Induction Schedule for ${prisonersWithoutInductionSchedule.size} prisoners were not created:
          $prisonersWithoutInductionSchedule
          
          Induction Schedules for ${prisonersWithCreatedInductionSchedule.size} prisoners were successfully created:
          $prisonersWithCreatedInductionSchedule
        """.trimIndent()
}

data class SchedulesEtlRollbackException(
  val schedulesEtlResponse: SchedulesEtlResponse,
) : RuntimeException()
