package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.MissingSentenceStartDateAndReceptionDateException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.PrisonEducationServiceProperties
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.aPrisonEducationServiceProperties
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class AssessmentServiceTest {
  @InjectMocks
  private lateinit var assessmentService: AssessmentService

  @Mock
  private lateinit var prisonerSearchApiService: PrisonerSearchApiService

  @Mock(lenient = true)
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
    private lateinit var receptionDate: LocalDate
    private var sentenceStartDate: LocalDate? = null

    @BeforeEach
    internal fun setUp() {
      receptionDate = pesContractStartDate.minusDays(1)
    }

    @Test
    fun `should not require Basic Skills Assessment`() {
      givenPrisoner()
      assertEquals(false, assessmentService.requireBasicSkillsAssessment(prisoner.prisonerNumber))
    }

    @Nested
    @DisplayName("And sentence start date is found")
    inner class AndSentenceStartDateFound {
      @Test
      fun `should not require Basic Skills Assessment, as sentence start before PES contract start`() {
        givenPrisoner(sentenceStartDate = pesContractStartDate.minusDays(1))
        assertEquals(false, assessmentService.requireBasicSkillsAssessment(prisoner.prisonerNumber))
      }

      @Test
      fun `should require Basic Skills Assessment, as sentence start after PES contract start`() {
        givenPrisoner(sentenceStartDate = pesContractStartDate)
        assertEquals(true, assessmentService.requireBasicSkillsAssessment(prisoner.prisonerNumber))
      }
    }

    private fun givenPrisoner(sentenceStartDate: LocalDate? = null) {
      this.sentenceStartDate = sentenceStartDate
      prisoner = aValidPrisoner(receptionDate = receptionDate).copy(sentenceStartDate = sentenceStartDate)
      given(prisonerSearchApiService.getPrisoner(prisoner.prisonerNumber)).willReturn(prisoner)
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

  @Nested
  @DisplayName("Given a prisoner with both sentence start date and reception date missing")
  inner class GivenPrisonerBothReceptionAndSentenceStartDatesMissing {
    private lateinit var prisoner: Prisoner

    @BeforeEach
    internal fun setUp() {
      prisoner = aValidPrisoner().copy(receptionDate = null, sentenceStartDate = null)
      given(prisonerSearchApiService.getPrisoner(prisoner.prisonerNumber)).willReturn(prisoner)
    }

    @Test
    fun `should throw exception`() {
      val prisonNumber = prisoner.prisonerNumber
      val expectedError = "Sentence start date and Reception date of Prisoner [$prisonNumber] are both missing."
      val exception = assertThrows(MissingSentenceStartDateAndReceptionDateException::class.java) {
        assessmentService.requireBasicSkillsAssessment(prisonNumber)
      }

      assertThat(exception.message).isEqualTo(expectedError)
      verify(pesProperities, never()).contractStartDate
    }
  }
}
