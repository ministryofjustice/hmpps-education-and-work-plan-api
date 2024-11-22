package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.LegalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.PagedPrisonerResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.PrisonerNotFoundException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.PrisonerSearchApiClient
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.PrisonerSearchApiException
import java.time.LocalDate

@ExtendWith((MockitoExtension::class))
class PrisonerSearchApiServiceTest {
  @InjectMocks
  private lateinit var service: PrisonerSearchApiService

  @Mock
  private lateinit var prisonerSearchApiClient: PrisonerSearchApiClient

  @Test
  fun `should get all prisoners in a given prison`() {
    // Given
    val prisonId = "BXI"

    val expectedPrisoners = listOf(
      Prisoner(
        prisonerNumber = "A1234BC",
        legalStatus = LegalStatus.SENTENCED,
        releaseDate = LocalDate.now().plusYears(1),
        prisonId = "BXI",
      ),
      Prisoner(
        prisonerNumber = "A9999XX",
        legalStatus = LegalStatus.SENTENCED,
        releaseDate = LocalDate.now().plusYears(2),
        prisonId = "BXI",
      ),
    )

    val pagedPrisonerResponse = PagedPrisonerResponse(true, expectedPrisoners)
    given(prisonerSearchApiClient.getPrisonersByPrisonId(any(), any(), any())).willReturn(pagedPrisonerResponse)

    // When
    val actual = service.getAllPrisonersInPrison(prisonId)

    // Then
    assertThat(actual).isEqualTo(expectedPrisoners)
    verify(prisonerSearchApiClient).getPrisonersByPrisonId("BXI", 0, 9999)
  }

  @Test
  fun `should not get all prisoners given prisoner search API returns an error`() {
    // Given
    val prisonId = "BXI"

    val expectedException = PrisonerSearchApiException(
      "Error retrieving prisoners by prisonId BXI",
      WebClientResponseException(500, "Service unavailable", null, null, null),
    )
    given(prisonerSearchApiClient.getPrisonersByPrisonId(any(), any(), any())).willThrow(expectedException)

    // When
    val exception = assertThrows(PrisonerSearchApiException::class.java) {
      service.getAllPrisonersInPrison(prisonId)
    }

    // Then
    assertThat(exception).isEqualTo(expectedException)
    verify(prisonerSearchApiClient).getPrisonersByPrisonId("BXI", 0, 9999)
  }

  @Test
  fun `should get prisoner by their prison number`() {
    // Given
    val prisonNumber = "A1234BC"
    val expectedPrisoner = Prisoner(
      prisonerNumber = "A1234BC",
      legalStatus = LegalStatus.SENTENCED,
      releaseDate = LocalDate.now().plusYears(1),
      prisonId = "BXI",
    )
    given(prisonerSearchApiClient.getPrisoner(any())).willReturn(expectedPrisoner)

    // When
    val actual = service.getPrisoner(prisonNumber)

    // Then
    assertThat(actual).isEqualTo(expectedPrisoner)
    verify(prisonerSearchApiClient).getPrisoner("A1234BC")
  }

  @Test
  fun `should not get prisoner given prisoner search API returns an error`() {
    // Given
    val prisonNumber = "A1234BC"

    val expectedException = PrisonerSearchApiException(
      "Error retrieving prisoner by prisonNumber A1234BC",
      WebClientResponseException(500, "Service unavailable", null, null, null),
    )
    given(prisonerSearchApiClient.getPrisoner(any())).willThrow(expectedException)

    // When
    val exception = assertThrows(PrisonerSearchApiException::class.java) {
      service.getPrisoner(prisonNumber)
    }

    // Then
    assertThat(exception).isEqualTo(expectedException)
    verify(prisonerSearchApiClient).getPrisoner("A1234BC")
  }

  @Test
  fun `should not get prisoner given prisoner search API returns a not found error`() {
    // Given
    val prisonNumber = "A1234BC"

    val expectedException = PrisonerNotFoundException(prisonNumber)
    given(prisonerSearchApiClient.getPrisoner(any())).willThrow(expectedException)

    // When
    val exception = assertThrows(PrisonerNotFoundException::class.java) {
      service.getPrisoner(prisonNumber)
    }

    // Then
    assertThat(exception).isEqualTo(expectedException)
    verify(prisonerSearchApiClient).getPrisoner("A1234BC")
  }
}
