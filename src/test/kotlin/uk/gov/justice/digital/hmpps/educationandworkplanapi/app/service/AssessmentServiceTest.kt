package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.PrisonEducationServiceProperties
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.aPrisonEducationServiceProperties

@ExtendWith(MockitoExtension::class)
class AssessmentServiceTest {
  @InjectMocks
  private lateinit var assessmentService: AssessmentService

  @Mock
  private lateinit var prisonerSearchApiService: PrisonerSearchApiService

  @Mock
  private lateinit var pesProperities: PrisonEducationServiceProperties

  private val pesContractStartDate = aPrisonEducationServiceProperties().contractStartDate

  @BeforeEach
  internal fun setUp() {
    given(pesProperities.contractStartDate).willReturn(pesContractStartDate)
  }

  @Nested
  @DisplayName("Given a prisoner with reception *before* PES contract start")
  inner class GivenPrisonerReceivedBeforePesContract {
    private lateinit var prisoner: Prisoner

    @BeforeEach
    internal fun setUp() {
      prisoner = aValidPrisoner(
        receptionDate = pesContractStartDate.minusDays(1),
        sentenceStartDate = pesContractStartDate.plusDays(1),
      )
      given(prisonerSearchApiService.getPrisoner(prisoner.prisonerNumber)).willReturn(prisoner)
    }

    @Test
    fun `should not require Basic Skills Assessment`() {
      val prisonNumber = prisoner.prisonerNumber
      val expected = false

      val actual = assessmentService.requireBasicSkillsAssessment(prisonNumber)

      assertEquals(expected, actual)
    }
  }

  @Nested
  @DisplayName("Given a prisoner with sentence start date *before* PES contract start")
  inner class GivenPrisonerWithSentenceStartBeforePesContract {
    private lateinit var prisoner: Prisoner

    @BeforeEach
    internal fun setUp() {
      prisoner = aValidPrisoner(
        receptionDate = pesContractStartDate.plusDays(1),
        sentenceStartDate = pesContractStartDate.minusDays(1),
      )
      given(prisonerSearchApiService.getPrisoner(prisoner.prisonerNumber)).willReturn(prisoner)
    }

    @Test
    fun `should not require Basic Skills Assessment`() {
      val prisonNumber = prisoner.prisonerNumber
      val expected = false

      val actual = assessmentService.requireBasicSkillsAssessment(prisonNumber)

      assertEquals(expected, actual)
    }
  }

  @Nested
  @DisplayName("Given a prisoner with both sentence start date and reception date on or *after* PES contract start")
  inner class GivenPrisonerBothReceptionAndSentenceStartDatesAfterPesContract {
    private lateinit var prisoner: Prisoner

    @BeforeEach
    internal fun setUp() {
      prisoner = aValidPrisoner(
        receptionDate = pesContractStartDate,
        sentenceStartDate = pesContractStartDate,
      )
      given(prisonerSearchApiService.getPrisoner(prisoner.prisonerNumber)).willReturn(prisoner)
    }

    @Test
    fun `should require Basic Skills Assessment`() {
      val prisonNumber = prisoner.prisonerNumber
      val expected = true

      val actual = assessmentService.requireBasicSkillsAssessment(prisonNumber)

      assertEquals(expected, actual)
    }
  }
}
