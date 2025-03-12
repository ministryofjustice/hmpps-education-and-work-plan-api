package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber

@ExtendWith(MockitoExtension::class)
class PrisonApiMapperTest {

  @InjectMocks
  private lateinit var mapper: PrisonApiMapper

  @Test
  fun `should convert to PrisonMovementEvents`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val prisonSummary = aValidPrisonerInPrisonSummary()
    val expected = aValidPrisonMovementEvents(
      prisonNumber = prisonNumber,
      prisonBookings = mapOf(
        1L to listOf(
          aValidAdmissionPrisonMovementEvent(),
          aValidReleasePrisonMovementEvent(),
          anotherValidAdmissionPrisonMovementEvent(),
          anotherValidReleasePrisonMovementEvent(),
          aValidTransferPrisonMovementEvent(),
          anotherValidTransferPrisonMovementEvent(),
        ),
        2L to listOf(
          anotherValidAdmissionPrisonMovementEvent(),
          anotherValidReleasePrisonMovementEvent(),
        ),
      ),
    )

    // When
    val actual = mapper.toPrisonMovementEvents(prisonNumber, prisonSummary)

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should convert to PrisonMovementEvents given empty prison periods`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val prisonSummary = aValidPrisonerInPrisonSummary(
      // not expected to happen in practice - there should always be an admission event
      prisonPeriod = emptyList(),
    )
    val expected = aValidPrisonMovementEvents(prisonNumber = prisonNumber, prisonBookings = emptyMap())

    // When
    val actual = mapper.toPrisonMovementEvents(prisonNumber, prisonSummary)

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should convert to PrisonMovementEvents given empty prison bookings`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val prisonSummary = aValidPrisonerInPrisonSummary(
      prisonPeriod = listOf(
        aValidPrisonPeriod(
          // not expected to happen in practice - there should always be an admission event
          movementDates = emptyList(),
          transfers = emptyList(),
        ),
      ),
    )
    val expected = aValidPrisonMovementEvents(
      prisonNumber = prisonNumber,
      prisonBookings = mapOf(
        1L to emptyList(),
      ),
    )

    // When
    val actual = mapper.toPrisonMovementEvents(prisonNumber, prisonSummary)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
