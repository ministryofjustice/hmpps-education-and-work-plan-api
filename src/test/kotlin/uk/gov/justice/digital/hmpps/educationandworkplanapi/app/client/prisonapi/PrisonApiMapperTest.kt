package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.prisonapi.resource.model.PrisonerInPrisonSummary
import java.time.LocalDate

class PrisonApiMapperTest {

  private var mapper = PrisonApiMapper()

  @Test
  fun `should map to PrisonMovementEvents`() {
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
  fun `should map to PrisonMovementEvents given empty prison periods`() {
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
  fun `should map to PrisonMovementEvents given empty prison bookings`() {
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

  @Test
  fun `should map to PrisonMovementEvents given bookings including Administrative movements`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()

    val objectMapper = ObjectMapper()
    val prisonApiResponse = """
{
  "prisonerNumber": "$prisonNumber",
  "prisonPeriod": [
    {
      "bookNumber": "15361B",
      "bookingId": 1473439,
      "bookingSequence": 8,
      "entryDate": "2018-01-06T15:13:08",
      "releaseDate": "2018-01-12T10:12:20",
      "movementDates": [
        {
          "reasonInToPrison": "Convicted Unsentenced",
          "dateInToPrison": "2018-01-06T15:13:08",
          "inwardType": "ADM",
          "reasonOutOfPrison": "NON CUSTODIAL SENTENCE",
          "dateOutOfPrison": "2018-01-12T10:12:20",
          "outwardType": "REL",
          "admittedIntoPrisonId": "WWI",
          "releaseFromPrisonId": "WWI"
        }
      ],
      "transfers": [],
      "prisons": [
        "WWI"
      ]
    },
    {
      "bookNumber": "78401B",
      "bookingId": 2405001,
      "bookingSequence": 7,
      "entryDate": "2018-09-13T00:00:00",
      "releaseDate": "2018-09-19T00:00:00",
      "movementDates": [
        {
          "reasonInToPrison": "Administrative",
          "dateInToPrison": "2018-09-13T00:00:00",
          "inwardType": "ADM",
          "reasonOutOfPrison": "Administrative Release due to Merge",
          "dateOutOfPrison": "2018-09-19T00:00:00",
          "outwardType": "REL"
        }
      ],
      "transfers": [],
      "prisons": []
    },
    {
      "bookNumber": "55332F",
      "bookingId": 2875329,
      "bookingSequence": 1,
      "entryDate": "2023-09-29T17:30:15",
      "movementDates": [
        {
          "reasonInToPrison": "Imprisonment Without Option",
          "dateInToPrison": "2023-09-29T17:30:15",
          "inwardType": "ADM",
          "reasonOutOfPrison": "CONDITIONAL RELEASE",
          "dateOutOfPrison": "2024-03-05T11:11:36",
          "outwardType": "REL",
          "admittedIntoPrisonId": "WWI",
          "releaseFromPrisonId": "BXI"
        },
        {
          "reasonInToPrison": "Unconvicted Remand",
          "dateInToPrison": "2024-06-12T19:08:57",
          "inwardType": "ADM",
          "reasonOutOfPrison": "Medical/Dental Outpatient Appointment",
          "dateOutOfPrison": "2024-11-21T09:50:04",
          "outwardType": "TAP",
          "admittedIntoPrisonId": "TSI",
          "releaseFromPrisonId": "BXI"
        },
        {
          "reasonInToPrison": "Medical/Dental Outpatient Appointment",
          "dateInToPrison": "2024-11-21T15:57:47",
          "inwardType": "TAP",
          "reasonOutOfPrison": "CONDITIONAL RELEASE",
          "dateOutOfPrison": "2025-02-20T17:38:47",
          "outwardType": "REL",
          "admittedIntoPrisonId": "BXI",
          "releaseFromPrisonId": "BXI"
        },
        {
          "reasonInToPrison": "Unconvicted Remand",
          "dateInToPrison": "2026-03-03T20:16:54",
          "inwardType": "ADM",
          "admittedIntoPrisonId": "TSI"
        }
      ],
      "transfers": [
        {
          "dateOutOfPrison": "2023-11-01T14:47:00",
          "dateInToPrison": "2023-11-01T15:41:02",
          "transferReason": "Normal Transfer",
          "fromPrisonId": "WWI",
          "toPrisonId": "BXI"
        },
        {
          "dateOutOfPrison": "2024-07-03T09:18:00",
          "dateInToPrison": "2024-07-03T15:25:02",
          "transferReason": "TRANSFER VIA COURT",
          "fromPrisonId": "TSI",
          "toPrisonId": "WWI"
        },
        {
          "dateOutOfPrison": "2024-11-18T13:17:00",
          "dateInToPrison": "2024-11-18T15:20:27",
          "transferReason": "Normal Transfer",
          "fromPrisonId": "WWI",
          "toPrisonId": "BXI"
        },
        {
          "dateOutOfPrison": "2026-04-07T13:48:00",
          "dateInToPrison": "2026-04-07T17:00:08",
          "transferReason": "Normal Transfer",
          "fromPrisonId": "TSI",
          "toPrisonId": "ONI"
        }
      ],
      "prisons": [
        "WWI",
        "BXI",
        "TSI",
        "ONI"
      ]
    }
  ]
}
    """.trimIndent()
    val prisonSummary = objectMapper.readValue(prisonApiResponse, PrisonerInPrisonSummary::class.java)

    val expected = aValidPrisonMovementEvents(
      prisonNumber = prisonNumber,
      prisonBookings = mapOf(
        1473439L to listOf(
          aValidAdmissionPrisonMovementEvent(
            date = LocalDate.parse("2018-01-06"),
            toPrisonId = "WWI",
          ),
          aValidReleasePrisonMovementEvent(
            date = LocalDate.parse("2018-01-12"),
            fromPrisonId = "WWI",
          ),
        ),
        2405001L to emptyList(), // Booking ID 2405001 contains only Administrative movements so is mapped to an empty list
        2875329L to listOf(
          aValidAdmissionPrisonMovementEvent(
            date = LocalDate.parse("2023-09-29"),
            toPrisonId = "WWI",
          ),
          aValidReleasePrisonMovementEvent(
            date = LocalDate.parse("2024-03-05"),
            fromPrisonId = "BXI",
          ),
          aValidAdmissionPrisonMovementEvent(
            date = LocalDate.parse("2024-06-12"),
            toPrisonId = "TSI",
          ),
          aValidReleasePrisonMovementEvent(
            date = LocalDate.parse("2025-02-20"),
            fromPrisonId = "BXI",
          ),
          aValidAdmissionPrisonMovementEvent(
            date = LocalDate.parse("2026-03-03"),
            toPrisonId = "TSI",
          ),
          aValidTransferPrisonMovementEvent(
            date = LocalDate.parse("2023-11-01"),
            fromPrisonId = "WWI",
            toPrisonId = "BXI",
          ),
          aValidTransferPrisonMovementEvent(
            date = LocalDate.parse("2024-07-03"),
            fromPrisonId = "TSI",
            toPrisonId = "WWI",
          ),
          aValidTransferPrisonMovementEvent(
            date = LocalDate.parse("2024-11-18"),
            fromPrisonId = "WWI",
            toPrisonId = "BXI",
          ),
          aValidTransferPrisonMovementEvent(
            date = LocalDate.parse("2026-04-07"),
            fromPrisonId = "TSI",
            toPrisonId = "ONI",
          ),
        ),
      ),
    )

    // When
    val actual = mapper.toPrisonMovementEvents(prisonNumber, prisonSummary)

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should map to PrisonMovementEvents given a booking includes both Administrative and non-Administrative movements`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()

    val objectMapper = ObjectMapper()
    val prisonApiResponse = """
{
  "prisonerNumber": "$prisonNumber",
  "prisonPeriod": [
    {
      "bookNumber": "55332F",
      "bookingId": 2875329,
      "bookingSequence": 1,
      "entryDate": "2023-09-29T17:30:15",
      "movementDates": [
        {
          "reasonInToPrison": "Imprisonment Without Option",
          "dateInToPrison": "2023-09-29T17:30:15",
          "inwardType": "ADM",
          "reasonOutOfPrison": "CONDITIONAL RELEASE",
          "dateOutOfPrison": "2024-03-05T11:11:36",
          "outwardType": "REL",
          "admittedIntoPrisonId": "WWI",
          "releaseFromPrisonId": "BXI"
        },
        {
          "reasonInToPrison": "Unconvicted Remand",
          "dateInToPrison": "2024-06-12T19:08:57",
          "inwardType": "ADM",
          "reasonOutOfPrison": "Medical/Dental Outpatient Appointment",
          "dateOutOfPrison": "2024-11-21T09:50:04",
          "outwardType": "TAP",
          "admittedIntoPrisonId": "TSI",
          "releaseFromPrisonId": "BXI"
        },
        {
          "reasonInToPrison": "Medical/Dental Outpatient Appointment",
          "dateInToPrison": "2024-11-21T15:57:47",
          "inwardType": "TAP",
          "reasonOutOfPrison": "CONDITIONAL RELEASE",
          "dateOutOfPrison": "2025-02-20T17:38:47",
          "outwardType": "REL",
          "admittedIntoPrisonId": "BXI",
          "releaseFromPrisonId": "BXI"
        },
        {
          "reasonInToPrison": "Administrative",
          "dateInToPrison": "2018-09-13T00:00:00",
          "inwardType": "ADM",
          "reasonOutOfPrison": "Administrative Release due to Merge",
          "dateOutOfPrison": "2018-09-19T00:00:00",
          "outwardType": "REL"
        },
        {
          "reasonInToPrison": "Unconvicted Remand",
          "dateInToPrison": "2026-03-03T20:16:54",
          "inwardType": "ADM",
          "admittedIntoPrisonId": "TSI"
        }
      ],
      "transfers": [
        {
          "dateOutOfPrison": "2023-11-01T14:47:00",
          "dateInToPrison": "2023-11-01T15:41:02",
          "transferReason": "Normal Transfer",
          "fromPrisonId": "WWI",
          "toPrisonId": "BXI"
        },
        {
          "dateOutOfPrison": "2024-07-03T09:18:00",
          "dateInToPrison": "2024-07-03T15:25:02",
          "transferReason": "TRANSFER VIA COURT",
          "fromPrisonId": "TSI",
          "toPrisonId": "WWI"
        },
        {
          "dateOutOfPrison": "2024-11-18T13:17:00",
          "dateInToPrison": "2024-11-18T15:20:27",
          "transferReason": "Normal Transfer",
          "fromPrisonId": "WWI",
          "toPrisonId": "BXI"
        },
        {
          "dateOutOfPrison": "2026-04-07T13:48:00",
          "dateInToPrison": "2026-04-07T17:00:08",
          "transferReason": "Normal Transfer",
          "fromPrisonId": "TSI",
          "toPrisonId": "ONI"
        }
      ],
      "prisons": [
        "WWI",
        "BXI",
        "TSI",
        "ONI"
      ]
    }
  ]
}
    """.trimIndent()
    val prisonSummary = objectMapper.readValue(prisonApiResponse, PrisonerInPrisonSummary::class.java)

    val expected = aValidPrisonMovementEvents(
      prisonNumber = prisonNumber,
      prisonBookings = mapOf(
        2875329L to listOf( // Booking ID 2875329 contains a mix of Administrative and non-Administrative movements, but only the non-Administrative movements are mapped and expected
          aValidAdmissionPrisonMovementEvent(
            date = LocalDate.parse("2023-09-29"),
            toPrisonId = "WWI",
          ),
          aValidReleasePrisonMovementEvent(
            date = LocalDate.parse("2024-03-05"),
            fromPrisonId = "BXI",
          ),
          aValidAdmissionPrisonMovementEvent(
            date = LocalDate.parse("2024-06-12"),
            toPrisonId = "TSI",
          ),
          aValidReleasePrisonMovementEvent(
            date = LocalDate.parse("2025-02-20"),
            fromPrisonId = "BXI",
          ),
          aValidAdmissionPrisonMovementEvent(
            date = LocalDate.parse("2026-03-03"),
            toPrisonId = "TSI",
          ),
          aValidTransferPrisonMovementEvent(
            date = LocalDate.parse("2023-11-01"),
            fromPrisonId = "WWI",
            toPrisonId = "BXI",
          ),
          aValidTransferPrisonMovementEvent(
            date = LocalDate.parse("2024-07-03"),
            fromPrisonId = "TSI",
            toPrisonId = "WWI",
          ),
          aValidTransferPrisonMovementEvent(
            date = LocalDate.parse("2024-11-18"),
            fromPrisonId = "WWI",
            toPrisonId = "BXI",
          ),
          aValidTransferPrisonMovementEvent(
            date = LocalDate.parse("2026-04-07"),
            fromPrisonId = "TSI",
            toPrisonId = "ONI",
          ),
        ),
      ),
    )

    // When
    val actual = mapper.toPrisonMovementEvents(prisonNumber, prisonSummary)

    // Then
    assertThat(actual).isEqualTo(expected)
  }
}
