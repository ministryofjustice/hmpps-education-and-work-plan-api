package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerMergedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation.Reason
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class PrisonerMergedEventServiceTest {
  @InjectMocks
  private lateinit var eventService: PrisonerMergedEventService

  @Mock
  private lateinit var reviewScheduleService: ReviewScheduleService

  @Mock
  private lateinit var inductionScheduleService: InductionScheduleService

  private val objectMapper = ObjectMapper()

  @Test
  fun `should process event given prisoner is merged`() {
    // Given
    val removedNomsNumber = randomValidPrisonNumber()

    val additionalInformation = aValidPrisonerMergedAdditionalInformation(
      prisonNumber = "A1234BC",
      reason = PrisonerMergedAdditionalInformation.Reason.MERGE,
      removedNomsNumber = removedNomsNumber,
    )
    val inboundEvent = anInboundEvent(additionalInformation)

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(reviewScheduleService).exemptActiveReviewScheduleStatusDueToMerge(
      prisonNumber = removedNomsNumber,
    )
  }

  private fun anInboundEvent(additionalInformation: PrisonerMergedAdditionalInformation): InboundEvent = InboundEvent(
    eventType = EventType.PRISONER_MERGED,
    personReference = PersonReference(listOf(Identifier(type = "noms", value = "A1234BC"))),
    occurredAt = Instant.now(),
    publishedAt = Instant.now(),
    description = "Prisoner merged event",
    version = "1.0",
    additionalInformation = objectMapper.writeValueAsString(additionalInformation),
  )
}
